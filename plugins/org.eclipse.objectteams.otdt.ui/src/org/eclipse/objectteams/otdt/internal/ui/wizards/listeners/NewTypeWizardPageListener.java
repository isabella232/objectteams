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
 * $Id: NewTypeWizardPageListener.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards.listeners;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.corext.refactoring.TypeContextChecker;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.SuperInterfaceSelectionDialog;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.search.OTSearchEngine;
import org.eclipse.objectteams.otdt.internal.ui.wizards.NewTeamWizardPage;
import org.eclipse.objectteams.otdt.internal.ui.wizards.NewTypeWizardPage;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.objectteams.otdt.ui.dialogs.TeamSelectionDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;


/**
 * The main listener class for org.eclipse.objectteams.otdt.internal.ui.wizards.NewTypeWizardPage.
 * It listens to changes of the entry fields and clicks on the browse buttons
 * of the observed NewTypeWizardPage.
 * 
 * @author kaschja
 * @version $Id: NewTypeWizardPageListener.java 23435 2010-02-04 00:14:38Z stephan $
 */
public abstract class NewTypeWizardPageListener implements IStringButtonAdapter,
                                                           IDialogFieldListener,
                                                           IListAdapter
{
	protected static final int CONTAINER       = 1;
	protected static final int PACKAGE         = 2;
	protected static final int ENCLOSINGTYPE   = 3;
	protected static final int NAME            = 4;
	protected static final int SUPERTYPE       = 5;
	protected static final int SUPERINTERFACES = 6;		
	protected static final int ENCLOSINGTYPESELECTION = 7;

	protected IStatus _packageStatus         = new StatusInfo();
	protected IStatus _enclosingTeamStatus   = new StatusInfo();
	protected IStatus _typeNameStatus        = new StatusInfo();
	protected IStatus _modifierStatus        = new StatusInfo();
	protected IStatus _superTypeStatus       = new StatusInfo();
	protected IStatus _superInterfacesStatus = new StatusInfo();

	private NewTypeWizardPage _observedPage;
	

    public NewTypeWizardPageListener(NewTypeWizardPage observedPage)
    {
    	_observedPage = observedPage;
    }

//------------------------------------------------------------------------------
    		
	// -------- IStringButtonAdapter
	/**
	 * gets called when a browse button of the observed wizard page is clicked
	 */
	public void changeControlPressed(DialogField field) 
	{		
		handleChangeControlPressed(field);
	}
	
	// -------- IDialogFieldListener
	/**
	 * gets called when the content of a dialog field of the observed wizard page
	 * has changed
	 */
	public void dialogFieldChanged(DialogField field) 
	{
		if (field == _observedPage.getTypeNameDialogField())
			_observedPage.publicTypeNameChanged(); // will eventually set _observedPage._currentType
		int changedElement = getChangedElement(field);
		performReviews(changedElement);
		updateStatus();
	}
	
	public void doubleClicked(ListDialogField field) 
	{
	}

    //-------- IListAdapter
    /**
     * gets called when the add interface button of the observed page is clicked
     */
	public void customButtonPressed(ListDialogField field, int index) 
	{
		chooseSuperInterfaces(field);
	}	
	public void selectionChanged(ListDialogField field) {}
	
//------------------------------------------------------------------------------	
    
    protected NewTypeWizardPage getObservedPage()
    {
	    return _observedPage;
    }    	
		
	protected int getChangedElement(DialogField field)
	{
	    int change = 0;
	    
		if (field == getObservedPage().getPackageDialogField())
		{
			change = PACKAGE;
		}
		else if (field == getObservedPage().getEnclosingTypeDialogField())
		{
			change = ENCLOSINGTYPE;
		}
		else if (field == getObservedPage().getInlineSelectionDialogField())
		{
		}
		else if (field == getObservedPage().getTypeNameDialogField())
		{
			change = NAME;
		}
		else if (field == getObservedPage().getSuperTypeDialogField())
		{
			change = SUPERTYPE;
		}
		else if (field == getObservedPage().getSuperInterfacesDialogField())
		{
			change = SUPERINTERFACES;
		}
		else if (field == getObservedPage().getEnclosingTypeSelectionField())
		{
			change = ENCLOSINGTYPESELECTION;
		}
		
		return change;
	}
	
	/**
	 * After calling this method, call updateStatus() to reflect the review-status in the UI.
	 * @param change The dialog field, that was changed, one of CONTAINER, PACKAGE, ENCLOSINGTYPE...
	 */
	protected void performReviews(int change)
    {
        switch (change)
        {
            case CONTAINER : 		
	            _packageStatus         = validatePackage();
				_enclosingTeamStatus   = validateEnclosingType();			
				_typeNameStatus        = validateTypeName();
				_superTypeStatus       = validateSuperType();
				_superInterfacesStatus = validateSuperInterfaces();
			break;
				
            case PACKAGE:
    			_packageStatus         = validatePackage();
				_enclosingTeamStatus   = validateEnclosingType();			
				_typeNameStatus        = validateTypeName();            
				break;
				
            case ENCLOSINGTYPE:
    			_enclosingTeamStatus   = validateEnclosingType();
				_typeNameStatus        = validateTypeName();
				break;
            case ENCLOSINGTYPESELECTION:
    			_packageStatus         = validatePackage();
    			_enclosingTeamStatus   = validateEnclosingType();
				_typeNameStatus        = validateTypeName();
				break;  
			case NAME:
				_typeNameStatus        = validateTypeName();
				break;

			case SUPERTYPE:
				_superTypeStatus       = validateSuperType();

			case SUPERINTERFACES:
				validateSuperInterfaces();
				
            default :
                break;
        }
    }

    protected void handleChangeControlPressed(DialogField field)
	{
		if (field == getObservedPage().getPackageDialogField()) 
		{
			IPackageFragment pack= choosePackage();	
			if (pack != null) 
			{
				getObservedPage().setPackageFragmentName(pack.getElementName());
			}
		}		
		else if (field == getObservedPage().getEnclosingTypeDialogField())
		{
			IType type= chooseEnclosingType();
			
			if (type != null)
			{
				String packageName = type.getPackageFragment().getElementName();
				getObservedPage().setPackageFragmentName(packageName);
				String qualifiedTeam = type.getFullyQualifiedName('.');
				getObservedPage().setEnclosingTypeName(qualifiedTeam);
			}
		}
		else if (field == getObservedPage().getSuperTypeDialogField())
		{
			IType supertype = chooseSuperType();
			
			if (supertype != null)
			{
				getObservedPage().setSuperTypeName(supertype.getFullyQualifiedName('.'));
			}							
		}
	}

	private IPackageFragment choosePackage() 
	{
		IPackageFragmentRoot froot    = getObservedPage().getPackageFragmentRoot();
		IJavaElement[]       packages = null;
		try 
		{
			if (froot != null && froot.exists()) 
			{
				packages= froot.getChildren();
			}
		} 
		catch (JavaModelException ex) 
		{
			OTDTUIPlugin.getExceptionHandler().logCoreException(null, ex);
		}
		
		if (packages == null) 
		{
			packages= new IJavaElement[0];
		}
		
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getObservedPage().getShell(), 
																		   new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT));
		dialog.setIgnoreCase(false);
		dialog.setTitle(NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_title);
		dialog.setMessage(NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_description);
		dialog.setEmptyListMessage(NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_empty); 
		dialog.setElements(packages);
		IPackageFragment pack = getObservedPage().getPackageFragment();
		
		if (pack != null) 
		{
			dialog.setInitialSelections(new Object[] { pack });
		}

		if (dialog.open() == ElementListSelectionDialog.OK) 
		{
			return (IPackageFragment) dialog.getFirstResult();
		}
		return null;
	}
	
	// TODO(SH): this method could easily be reused by clients, look for better location then.
	public static IType chooseTeam(IJavaElement searchScope, Shell shell, IWizardContainer container, String dialogTitle, String dialogMessage, String filter) 
	{
		IJavaSearchScope scope= OTSearchEngine.createOTSearchScope(new IJavaElement[] { searchScope }, true);
		TeamSelectionDialog dialog = new TeamSelectionDialog(shell, container, scope);
		dialog.setTitle(dialogTitle);
		dialog.setMessage(dialogMessage);
		dialog.setFilter(filter);

	
		if (dialog.open() == Window.OK) 
			return (IType) dialog.getFirstResult();

		return null;
	}
	
	protected abstract IType chooseSuperType();
	
	private void chooseSuperInterfaces(ListDialogField field) 
	{
		IPackageFragmentRoot root= getObservedPage().getPackageFragmentRoot();
		if (root == null) 
		{
			return;
		}	

		SuperInterfaceSelectionDialog dialog;
		 
		
		Shell parent = getObservedPage().getShell();
		IRunnableContext context =  getObservedPage().getWizard().getContainer();
		NewTypeWizardPage page = getObservedPage(); 
		IJavaProject root2 = root.getJavaProject();
	
		dialog = new SuperInterfaceSelectionDialog(parent, context, page, root2);
/*		dialog = new SuperInterfaceSelectionDialog(getObservedPage().getShell(), 
												   getObservedPage().getWizard().getContainer(), 
												   getObservedPage(), 
												   root.getJavaProject());
*/		dialog.setTitle( NewWizardMessages.NewTypeWizardPage_InterfacesDialog_class_title);
		dialog.setMessage(NewWizardMessages.NewTypeWizardPage_InterfacesDialog_message);
		dialog.open();
		return;
	}	

	protected IType chooseEnclosingType() 
	{
		return getObservedPage().chooseEnclosingType();
	}		
	
    protected boolean hasErrorStatus(IStatus[] stati)
    {
        for (int i = 0; i < stati.length; i++)
        {
            if (stati[i] != null && stati[i].getSeverity() == IStatus.ERROR)
                return true;
        }
        
        return false;
    }

	public abstract IStatus[] getRelevantStates(boolean ignoreFirstField);

    protected void updateStatus()
    {
		// the most severe status will be displayed and the ok button enabled/disabled.
		getObservedPage().updateStatus(getRelevantStates(false));    	 
    }

    /**
     * Gets called when the type name has changed. The method validates the 
     * type name and returns the status of the validation.
     * 
     * @return the status of the validation
     */
    protected IStatus validateTypeName()
    {
        StatusInfo status = new StatusInfo();
        String typeName = getObservedPage().getTypeName();        
        
        // must not be empty
        if (typeName.length() == 0)
        {
            status.setError(NewWizardMessages.NewTypeWizardPage_error_EnterTypeName);
            return status;
        }
        if (typeName.indexOf('.') != -1)
        {
            status.setError(NewWizardMessages.NewTypeWizardPage_error_QualifiedName); 
            return status;
        }
        IStatus val = NewTypeWizardPage.validateJavaTypeName(typeName, getObservedPage().getJavaProject());
        if (val.getSeverity() == IStatus.ERROR)
        {
            status.setError(Messages.format(NewWizardMessages.NewTypeWizardPage_error_InvalidTypeName, val.getMessage())); 
            return status;
        }
        else if (val.getSeverity() == IStatus.WARNING)
        {
            status.setWarning(Messages.format(NewWizardMessages.NewTypeWizardPage_warning_TypeNameDiscouraged, val.getMessage())); 
            // continue checking
        }

        // role file must not exist        
        IType enclosingTeam = getObservedPage().getEnclosingType();
        IPackageFragment pack = getObservedPage().getPackageFragment();
        if ((pack == null) && (enclosingTeam != null)) // manually create package via team
        {
            pack = getTeamPackage(enclosingTeam);
        }
        
        if (pack != null)
        {
            ICompilationUnit cu = pack.getCompilationUnit(typeName + ".java"); //$NON-NLS-1$
            if (cu.getResource().exists())
            {
                status.setError(NewWizardMessages.NewTypeWizardPage_error_TypeNameExists);
                return status;
            }
        }
    	// inlined role must not exist
        if (enclosingTeam != null)
        {
            IType member = enclosingTeam.getType(typeName);
            {
                if (member.exists())
                {
                    status.setError(NewWizardMessages.NewTypeWizardPage_error_TypeNameExists);
                    return status;
                }
            }
        }
        
        validateSuperClass(status);
        
        return status;
    }

    /**
     * @param teamType the team (maybe nested) to look up the team package for
     * @return the team package, which may not physically exist or null if an exception occurred.
     */
    private IPackageFragment getTeamPackage(IType teamType)
    {
        String enclosingTeamName = teamType.getTypeQualifiedName('/'); // e.g. "Outer/Inner/MostInner"
        try {
            IPath teamPackagePath = teamType.getPackageFragment().getPath();
            teamPackagePath = teamPackagePath.append(enclosingTeamName);
            return teamType.getJavaProject().findPackageFragment(teamPackagePath);
        }
        catch (JavaModelException ex)
        {
            // package does not exist
        }
        return null;
    }

  /**
   * Gets called when the enclosing type name has changed. The method 
   * validates the enclosing type and returns the status of the validation. 
   * It also updates the enclosing type model.
   * 
   * @return the status of the validation
   */ 
	protected IStatus validateEnclosingType()
	{
	    StatusInfo status = new StatusInfo();
	    getObservedPage().setEnclosingType(null, true);
	
	    IPackageFragmentRoot root = getObservedPage().getPackageFragmentRoot();
	    if (root == null)
	    {
	        status.setError(""); //$NON-NLS-1$
	        return status;
	    }
	
	    String enclName = getObservedPage().getEnclosingTypeName();
	    if (enclName.trim().length() == 0)
	    {   
	    	handleEnclosingTypeDialogFieldIsEmpty(status);
			return status;
	    }
	    else
	    {
	        if (_observedPage instanceof NewTeamWizardPage)
	        {
	            _observedPage.getPackageDialogField().setEnabled(false);
	        }
	    }

	    try
	    {
	        IType type = root.getJavaProject().findType(enclName);
	        
	        if (type == null)
	        {
	            status.setError(NewWizardMessages.NewTypeWizardPage_error_EnclosingTypeNotExists); 
	        }
	        else if (type.getCompilationUnit() == null)
	        {
	            status.setError(NewWizardMessages.NewTypeWizardPage_error_EnclosingNotInCU);
	        }
	        else if (!JavaModelUtil.isEditable(type.getCompilationUnit()))
	        {
	            status.setError(NewWizardMessages.NewTypeWizardPage_error_EnclosingNotEditable);
	        }
	        else if (type.equals(getObservedPage().getSuperType()))
	        {
	        	status.setError(org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages.NewTypeWizardPage_same_enclosing_and_super_error);
	        }
	        
	        if (status.isError())
	        {
				return status;	
	        }
	
	        getObservedPage().setEnclosingType(type, true);
	        
	        IPackageFragmentRoot enclosingRoot = JavaModelUtil.getPackageFragmentRoot(type);
	        if (!enclosingRoot.equals(root))
	        {
	            status.setWarning(NewWizardMessages.NewTypeWizardPage_warning_EnclosingNotInSourceFolder);
	        }
	        
	        validateSuperClass(status);
	        
			//enable Inlined-Checkbox
			getObservedPage().getInlineSelectionDialogField().setEnabled(true);
			return status;
	    }
	    catch (JavaModelException ex)
	    {
	        status.setError(NewWizardMessages.NewTypeWizardPage_error_EnclosingTypeNotExists);
            OTDTUIPlugin.getExceptionHandler().logCoreException("", ex);	         //$NON-NLS-1$
	        return status;
	    }
	}

	protected void handleEnclosingTypeDialogFieldIsEmpty(StatusInfo status)
	{
	    validateSuperClass(status);
	}
         
	/**
	 * Gets called when the package field has changed. The method 
	 * validates the package name and returns the status of the validation.
	 * The validation also updates the package fragment model.
	 * 
	 * @return the status of the validation
	 */
	protected IStatus validatePackage() { 
		return new StatusInfo(); // default: nothing to check
	}

	/**
	 * Gets called when the list of super interface has changed. The method 
	 * validates the superinterfaces and returns the status of the validation.
	 * 
	 * @return the status of the validation
	 */
    protected IStatus validateSuperInterfaces()
    {
        StatusInfo status = new StatusInfo();

        IPackageFragmentRoot root = getObservedPage().getPackageFragmentRoot();
        getObservedPage().getSuperInterfacesDialogField().enableButton(
            0,
            root != null);

        if (root != null)
        {
            List elements = getObservedPage().getSuperInterfaces();
            int nElements = elements.size();
            for (int i = 0; i < nElements; i++)
            {
                String intfname = (String) elements.get(i);
                Type type= TypeContextChecker.parseSuperInterface(intfname);
				if (type == null) {
					status.setError(Messages.format(NewWizardMessages.NewTypeWizardPage_error_InvalidSuperInterfaceName, intfname)); 
					return status;
				}
            }
        }
        return status;
    }

	/**
	 * Gets called when the superclass name has changed. The method 
	 * validates the superclass name and returns the status of the validation.
	 * 
	 * @return the status of the validation
	 */
    protected IStatus validateSuperType()
    {
        StatusInfo           status = new StatusInfo();
        IPackageFragmentRoot root   = getObservedPage().getPackageFragmentRoot();
        getObservedPage().getSuperTypeDialogField().enableButton(root != null);

        getObservedPage().setSuperType(null);

        String superTypeName = getObservedPage().getSuperTypeName();
        if (superTypeName.trim().length() == 0)
        {
            // accept the empty field (stands for java.lang.Object)
            return status;
        }
        IStatus val = NewTypeWizardPage.validateJavaTypeName(superTypeName, getObservedPage().getJavaProject());
        if (val.getSeverity() == IStatus.ERROR)
        {
            status.setError(NewWizardMessages.NewTypeWizardPage_error_InvalidSuperClassName);
            return status;
        }
        if (root != null)
        {
            try
            {
    			Type type= TypeContextChecker.parseSuperClass(superTypeName);
    			if (type == null) {
    				status.setError(NewWizardMessages.NewTypeWizardPage_error_InvalidSuperClassName); 
    				return status;
    			} 
    			IType iType = resolveSuperTypeName(root.getJavaProject(), superTypeName);
    			if (iType == null) {
    				status.setError(NewWizardMessages.NewTypeWizardPage_error_InvalidSuperClassName); 
    				return status;
    			} 
    			if (iType.equals(getObservedPage().getEnclosingType()))
    	        {
    	        	status.setError(org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages.NewTypeWizardPage_same_enclosing_and_super_error);
                }
                getObservedPage().setSuperType(iType);
                validateSuperClass(status);
            }
            catch (JavaModelException e)
            {
                status.setError(NewWizardMessages.NewTypeWizardPage_error_InvalidSuperClassName); 
                JavaPlugin.log(e);
            }
        }
        else
        {
            status.setError(""); //$NON-NLS-1$
        }
        return status;
    }

    private IType resolveSuperTypeName(IJavaProject jproject, String supertypeName)
        throws JavaModelException
    {
        if (!jproject.exists())
        {
            return null;
        }
        IType supertype = null;
 
        //TODO (kaschja): handle case of external defined role class        
		//innerclass (internal defined role class)
		if ( (getObservedPage().getEnclosingType() != null)
			&& (getObservedPage().isInlineTypeSelected()) )        	
    	{
            // search in the context of the enclosing type
            IType enclosingType = getObservedPage().getEnclosingType();
          
            String[][] res = enclosingType.resolveType(supertypeName);
            if (res != null && res.length > 0)
            {
				supertype = jproject.findType(res[0][0], res[0][1]);
            }
    	}
        else
        {
            IPackageFragment currPack = getObservedPage().getPackageFragment();
            if (currPack != null)
            {
                String packName = currPack.getElementName();
                // search in own package
                if (!currPack.isDefaultPackage())
                {
					supertype = jproject.findType(packName, supertypeName);
                }
                // search in java.lang
                if (supertype == null && !"java.lang".equals(packName)) //$NON-NLS-1$
                { 
					supertype = jproject.findType("java.lang", supertypeName); //$NON-NLS-1$
                }
            }
            // search fully qualified
            if (supertype == null)
            {
				supertype = jproject.findType(supertypeName);
            }
        }       
        return supertype;
    }

	/**
	 * workaround
	 * 
	 * The containerDialogField of the observed NewTypeWizardPage
	 * is defined in a superclass and declared private. That's why this listener
	 * cannot be registered to this dialog field. So its dialogFieldChanged method
	 * does not get called when the container dialog field has changed.
	 * Fortunately the observed NewTypeWizardPage gets informed about changes of
	 * this dialog field via its superclass hook method handleFieldChanged.
	 * So it can inform its listener by calling this method.
	 * 
	 * simulates call of handleDialogFieldChanged
	 */
	public void handleContainerChanged()
	{
		//TODO (kaschja): check if the container is inside a or itself an OTProject		
		//		getObservedPage().getPackageFragmentRoot().getJavaProject().getProject().getNature(natureId);
		
		performReviews(CONTAINER);
		updateStatus();		
	}
	
	
	void validateSuperClass(StatusInfo status)
	{
	    
	    IType superType      = getObservedPage().getSuperType();
	    IType enclosingType = getObservedPage().getEnclosingType();
	    String newRoleName = getObservedPage().getTypeName().trim();
        
	    IOTType superTypeOTElem        = null;
	    
	    if (superType == null)
	        return;
	    
	    superTypeOTElem = OTModelManager.getOTElement(superType);
	    
	    try {
		    if ( (superTypeOTElem != null) && superTypeOTElem.isRole() ) 
		    {
		        checkSuperRoleType(status, enclosingType, superType, newRoleName);
		        
		        if (status.isError())
		            return;
		    }

		    // TODO (carp): check if this works
// old TODO (kaschja) comment the following section in by the time IType.newSuperTypeHierarchy works properly for role classes
//	        if ( isTypeToBeCreatedARole() && ! newRoleName.equals("") )
//	        {   
//	            checkImplicitRoleHierarchyForExplicitSuperClass(status, enclosingType, superType, newRoleName);
//	        }
	        
	    }
	    catch (JavaModelException ex) {
	        ex.printStackTrace();
	    }
	}
	

	private void checkSuperRoleType(StatusInfo status, IType enclosingType, IType superType, String newRoleName) throws JavaModelException
	{
        if ( ! isTypeToBeCreatedARole() )
        {
            status.setError(org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages.NewTypeWizardPage_super_of_regular_is_role_error);
            return;
        }
        
        if ( ! isSuperRolesTeamContainedInEnclosingTypesHierarchy(enclosingType, superType) )
        {
            status.setError(org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages.NewRoleWizardPage_super_is_role_of_different_team_error);
            return;
        }
        
        if ( newRoleName.equals( superType.getElementName() ) )
        {
            status.setError(org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages.NewRoleWizardPage_explicit_and_implicit_subclassing_error);
            return;
        }
        
        if ( hasSuperroleOverridingImplicitSubrole(enclosingType, superType) )
        {
            status.setError(org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages.NewRoleWizardPage_super_is_overridden_error);
            return;
        }	    
	}
	
	/**
	 * checks if the role that is to be created
	 * has already an explicit superclass
	 * (that is the role that is to be created has an implicit superrole which has an explicit superclass)
	 * and if so
	 * the class that is intended to be the explicit superclass of the role that is to be created
	 * must be a subclass of the existing explicit superclass
	 * otherwise the status is set to ERROR 
	 * 
	 * FIXME(SH): add a call to this method!
	 */     
	private void checkImplicitRoleHierarchyForExplicitSuperClass(StatusInfo status, IType enclosingType, IType superType, String newRoleName)
		throws JavaModelException
	{
        IType explicitSuperclassOfImplicitSuperrole = getExplicitSuperclassOfImplicitSuperrole(enclosingType, newRoleName);
        
        if (explicitSuperclassOfImplicitSuperrole != null)
        {
            if (explicitSuperclassOfImplicitSuperrole.getFullyQualifiedName().equals(superType.getFullyQualifiedName()))
            {
                status.setInfo(Messages.format(
                		org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages.NewRoleWizardPage_already_has_this_super,
                		superType.getFullyQualifiedName()));
                return;
            }
       
            ITypeHierarchy superTypeHierarchy = superType.newSupertypeHierarchy(new NullProgressMonitor());
            
            if ( !superTypeHierarchy.contains(explicitSuperclassOfImplicitSuperrole))
            {
                status.setError(Messages.format(
                		org.eclipse.objectteams.otdt.internal.ui.wizards.OTNewWizardMessages.NewRoleWizardPage_incompatible_supers_error,
                        explicitSuperclassOfImplicitSuperrole.getFullyQualifiedName()));
                return;
            }          
        }	    
	}
	
	private IType getExplicitSuperclassOfImplicitSuperrole(IType teamType, String roleSimpleName) throws JavaModelException
	{
	    IOTType teamOTElem = OTModelManager.getOTElement(teamType);
	    
	    if ( (teamOTElem == null) || (!teamOTElem.isTeam()) )
	    {
	        return null;
	    }
	    
	    ITypeHierarchy teamSuperHierarchy = teamType.newSupertypeHierarchy(null);
	    IType[]           superTeams            = teamSuperHierarchy.getAllSuperclasses(teamType);
	    IType             curRole                  = null;
	    ITypeHierarchy explicitRoleSuperHierarchy = null;
	    
	    for (int idx = 0; idx < superTeams.length; idx++)
        {
            curRole = superTeams[idx].getType(roleSimpleName);
            if (curRole.exists())
            {
                explicitRoleSuperHierarchy = curRole.newSupertypeHierarchy(null);
                if (explicitRoleSuperHierarchy.getSuperclass(curRole) != null)
                {
                    return explicitRoleSuperHierarchy.getSuperclass(curRole);
                }
            }
        }
	    return null;
	}
	
	private boolean isTypeToBeCreatedARole()
	{
	    IType enclosingType = getObservedPage().getEnclosingType();
	    
        if (enclosingType == null)
        {
           return false;
        }
        IOTType enclosingTypeOTElem = OTModelManager.getOTElement(enclosingType);
        
        if ( (enclosingTypeOTElem == null) || ( ! enclosingTypeOTElem.isTeam()) )
        {
            return false;
        }
        return true;
	}
	
	private boolean isSuperRolesTeamContainedInEnclosingTypesHierarchy(IType enclosingTeam, IType superRole) 
		throws JavaModelException
	{
        ITypeHierarchy teamHierarchy     = enclosingTeam.newSupertypeHierarchy(null);
        IType             teamOfSuperType = superRole.getDeclaringType();

        if ( ! teamHierarchy.contains(teamOfSuperType) )
        {
            return false;
        }
        return true;
	}
	
	private boolean hasSuperroleOverridingImplicitSubrole(IType enclosingTeam, IType superRole) throws JavaModelException
	{
        IType teamOfSuperrole = superRole.getDeclaringType();
        
        if (enclosingTeam.getFullyQualifiedName().equals(teamOfSuperrole.getFullyQualifiedName()))
        {
            return false;
        }
        
        ITypeHierarchy enclosingTeamHierarchy = enclosingTeam.newSupertypeHierarchy(null);
        IType[]           superTeams                 = enclosingTeamHierarchy.getAllSuperclasses(enclosingTeam);
        IType             implicitSubrole              = null;
        int                 idx                             = 0;
        IType             curTeam                      = superTeams[idx];

        while ( ! curTeam.getFullyQualifiedName().equals(teamOfSuperrole.getFullyQualifiedName()))
        {
            implicitSubrole = curTeam.getType(superRole.getElementName());
            if (implicitSubrole.exists())
            {
                return true;
            }
            idx++;
            curTeam = superTeams[idx];
        }
        return false;
	}	
}
