/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AddTypeBindingDialog.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.bindingeditor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * This dialog is for selection of a role class and a base class for editing.
 * 
 * @author $ikeman$
 * @version $Id:AddTypeBindingDialog.java 15586 2007-03-23 13:07:39Z stephan $
 */
public class AddTypeBindingDialog extends FilteredTypesSelectionDialog
{
	private boolean _baseRelativeToEnclosingBase = false;
    private IType _baseType;
    private IType _roleType;
    private IType _focusTeam;
    private String _roleTypeName;
    private String _baseTypeName;

    private IType[] _roleTypes;
    private FilteredList _rolList;
    private String _roleNamePattern = ""; //$NON-NLS-1$

    private Button _okButton;
    private IJavaSearchScope _currentSearchScope; // FIXME(SH) (see showOnlyBaseOfEnclosing())
    
    public class LabelProvider extends WorkbenchLabelProvider implements ITableLabelProvider
	{
        	protected String decorateText(String input, Object element)
        	{
        		IType role =  (IType)element;
        		String name = role.getTypeQualifiedName().replace('$','.');
        	    return name;
        	}
		
		public void addListener(ILabelProviderListener listener)
		{
		}

		public void dispose()
		{
		}

		public boolean isLabelProperty(Object element, String property)
		{
			return false;
		}

		public void removeListener(ILabelProviderListener listener)
		{
		}

		public Image getColumnImage(Object element, int columnIndex)
		{
			return null;
		}

		public String getColumnText(Object element, int columnIndex)
		{
			return null;
		}
	}
    
	
	static AddTypeBindingDialog create(Shell parentshell, IType focusTeam) {
		IJavaSearchScope scope = createSearchScope(focusTeam, true);
		return new AddTypeBindingDialog(parentshell, focusTeam, scope);
	}
    private AddTypeBindingDialog(Shell parentshell, IType focusTeam, IJavaSearchScope scope)
    {
    	super(parentshell, false, null, scope, IJavaSearchConstants.TYPE);
    	_currentSearchScope = scope; 
        _focusTeam = focusTeam;

        setMessage(
                OTDTUIPlugin.getResourceString("BindingEditor.AddconnectorDialog.BaseClassSelect.Input.title")); //$NON-NLS-1$
    }
    
    protected static IJavaSearchScope createSearchScope(IType type, boolean onlyEnclosingBase)
    {
    	IJavaSearchScope scope = null;
    	
    	if (onlyEnclosingBase)
    	{
    		IRoleType enclosingRole = null;
    		if (type instanceof IRoleType) // shouldn't OTModelManager.getOTElement() handle that case?
    			enclosingRole = (IRoleType) type;
    		else
    		{
    			IOTType enclosingType = (IOTType) OTModelManager.getOTElement(type);
    			if (enclosingType instanceof IRoleType)
    				enclosingRole = (IRoleType) enclosingType; 
    		}
    		
    		if (enclosingRole != null)
    		{
				try {
	    			IType baseType = enclosingRole.getBaseClass();
					scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {baseType}, false);
				} catch (JavaModelException ex) {
					OTDTUIPlugin.getExceptionHandler().logCoreException("Cannot create enclosing-base-searchscope", ex); //$NON-NLS-1$
				}
    		}
    		// else flag an error? for now, just create the other scope
    	}
    	
    	if (scope == null)
			scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getJavaProject()}, true);
    	
    	return scope;
    }

    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(OTDTUIPlugin.getResourceString("BindingEditor.AddconnectorDialog.title")); //$NON-NLS-1$
    }
        
    public Control createDialogArea(Composite parent)
    {
    	Group panes = new Group(parent, SWT.NONE);
    	panes.setLayout(new GridLayout(2, true));
    	panes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Group roleTypeGrp   = createRoleGroup(panes);
        createRoleLabel(roleTypeGrp);
        createRoleTypeText(roleTypeGrp);
        createRoleListLabel(roleTypeGrp);
        createRoleList(roleTypeGrp);        
        
        Group baseTypeGrp   = createBaseGroup(panes);
    	super.createDialogArea(baseTypeGrp);
        createSrchInEncBaseCheck(baseTypeGrp);

        return panes;
    }

    private Group createRoleGroup(Composite container)
    {
        Group roleClsGrp = new Group(container, SWT.SHADOW_NONE);
        roleClsGrp.setText(OTDTUIPlugin.getResourceString(
                    "BindingEditor.AddconnectorDialog.RoleClassSelect.title")); //$NON-NLS-1$
        roleClsGrp.setLayout(new GridLayout());
		roleClsGrp.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        return roleClsGrp;
    }

    private void createRoleLabel(Group roleTypeGrp)
    {
        Label rolPatLabel = new Label(roleTypeGrp, SWT.NONE);
        rolPatLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rolPatLabel.setText(
                OTDTUIPlugin.getResourceString("BindingEditor.AddconnectorDialog.RoleClassSelect.Input.title")); //$NON-NLS-1$
    }

    private void createRoleTypeText(Group roleTypeGrp)
    {
        final Text result = new Text(roleTypeGrp, SWT.BORDER);
        result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        result.setText((_roleNamePattern == null ? "" : _roleNamePattern)); //$NON-NLS-1$
        
		result.addListener(SWT.Modify, new Listener()
		{
			public void handleEvent(Event evt)
			{
			    _rolList.setFilter(result.getText());
			    checkOkButton();
			}
		});
		result.addKeyListener(new KeyListener() 
         {
			public void keyPressed(KeyEvent evt)
			{
				if (evt.keyCode == SWT.ARROW_DOWN)
				    _rolList.setFocus();
			}

			public void keyReleased(KeyEvent evt)
			{
			    checkOkButton();
			}
		});
    }

    private void createRoleListLabel(Group roleTypeGrp)
    {
        Label result = new Label(roleTypeGrp, SWT.NONE);
		result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        result.setText(
                OTDTUIPlugin.getResourceString("BindingEditor.AddconnectorDialog.RoleClassSelect.List.title")); //$NON-NLS-1$
    }
    
    private void createRoleList(Group roleGroup)
    {
        FilteredList result = new FilteredList(
                                    roleGroup,
                                    SWT.BORDER,
                                    new LabelProvider(), 
                                    true, 
                                    false, 
                                    true);
        GridData gd = new GridData(GridData.FILL_BOTH);
		PixelConverter converter= new PixelConverter(result);
		gd.widthHint= converter.convertWidthInCharsToPixels(70);
		gd.heightHint= convertHeightInCharsToPixels(10);		
        result.setLayoutData(gd);
        result.setElements(_roleTypes); // computed by open()->computeRoles()
        _rolList = result;
    }

	private Group createBaseGroup(Composite container)
    {
        final Group result = new Group(container, SWT.TOP);
        result.setText(OTDTUIPlugin.getResourceString(
                "BindingEditor.AddconnectorDialog.BaseClassSelect.title")); //$NON-NLS-1$
        result.setLayout(new GridLayout());
        result.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        return result;
    }
    
    private void createSrchInEncBaseCheck(Group baseTypeGrp) {
	    final Button srchInEncBaseCheck = new Button(baseTypeGrp, SWT.CHECK);
		srchInEncBaseCheck.setText(OTDTUIPlugin.getResourceString("BindingEditor.AddTypeBindingDialog.use_base_as_anchor")); //$NON-NLS-1$
		srchInEncBaseCheck.setEnabled(false);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        srchInEncBaseCheck.setLayoutData(gd);
		try {
	        IOTType teamType = null;
	        if (_focusTeam != null)
	            teamType = OTModelManager.getOTElement(_focusTeam);
			
			if (teamType != null && teamType.isRole()) {
	        	IType baseclass = ((IRoleType)teamType).getBaseClass();
	        	if (baseclass != null) {
		        	srchInEncBaseCheck.setText(Messages.format(
		        			OTDTUIPlugin.getResourceString("BindingEditor.AddTypeBindingDialog.use_specified_base_as_anchor"), //$NON-NLS-1$
		        			baseclass.getElementName()));
		        	srchInEncBaseCheck.setEnabled(true);
			        srchInEncBaseCheck.addSelectionListener( new SelectionAdapter() 
			        {
			            public void widgetSelected(SelectionEvent evt)
			            {
			            	showOnlyBaseOfEnclosing(srchInEncBaseCheck.getSelection());
			            }
			        });
	        	}
	        } 
	    } catch (JavaModelException jme) {
	    	// no change
	    }
    }	
    
	private void showOnlyBaseOfEnclosing(boolean onlyBase) 
	{
		// FIXME(SH): cannot affect scope of super class TypeSelectionDialog2!!
		_currentSearchScope = createSearchScope(_focusTeam, onlyBase);
		_baseRelativeToEnclosingBase = onlyBase;
	} 
    
    protected void createButtonsForButtonBar(Composite parent)
    {
        _okButton = createButton(
                parent,
                IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL,
                true);
        createButton(
            parent,
            IDialogConstants.CANCEL_ID,
            IDialogConstants.CANCEL_LABEL,
            false);
        
        _okButton.setEnabled(false);
    }

    private void checkOkButton()
    {
    	// Note(SH): seemingly called only when base selection has been made.
        if (_rolList.isEmpty() || _rolList.getSelectionIndex()== -1)
            _okButton.setEnabled(false);
        else
            _okButton.setEnabled(true);
    }
    
	protected void updateButtonsEnableState(IStatus status)
	{
	    if (!_rolList.isEmpty())
	        super.updateButtonsEnableState(status);
	}
	
	@Override
	protected void handleSelected(StructuredSelection selection) {
		super.handleSelected(selection);
		checkOkButton();
	}
	
	@Override
	protected void handleDoubleClick() {
		//overwrite inherited method, just do nothing on a doubleclick on a type
	}
	
	@Override
	public int open()
	{
		computeRoles();
		if (_roleTypes == null || _roleTypes.length == 0)
        {
			String title= org.eclipse.objectteams.otdt.internal.ui.bindingeditor.Messages.AddTypeBindingDialog_role_selection_title;
			String message= org.eclipse.objectteams.otdt.internal.ui.bindingeditor.Messages.AddTypeBindingDialog_no_roles_available_error;
			MessageDialog.openInformation(getShell(), title, message);
			return CANCEL;
		}
		return super.open();
	}
	
	private void computeRoles() {
        IOTType teamType = null;
        if (_focusTeam != null)
        {
            teamType = OTModelManager.getOTElement(_focusTeam);
//{Experimental: look for roles inherited from explicit _and_ implicit superteam
            IType[] roles = null;
//            ArrayList roles = new ArrayList();
            try 
            {
                // roles = TypeHelper.getInheritedRoleTypes(team);
                roles = teamType.getRoleTypes(IOTType.ALL);
                // FIXME(SH): filter overridden tsupers.
//                if (team.isRole()) {
//                	ITypeHierarchy tsuperHierarchy = ((IRoleType)team).newImplicitSupertypeHierarchy();
//                	IType[] tsuperTeams = tsuperHierarchy.getAllClasses();
//                	for (int i = 0; i < tsuperTeams.length; i++) {
//						roles.addAll(Arrays.asList(TypeHelper.getRoleTypes(tsuperTeams[i], false)));
//					}
//                } else {
//                	roles.addAll(Arrays.asList(TypeHelper.getRoleTypes(team, false)));
//                }
                IType javaTeam = (IType)teamType.getCorrespondingJavaElement();
                IType[] relevant = new IType[roles.length];
                int j=0;
                for (IType role : roles) {
					IOTType roleType = (IOTType)OTModelManager.getOTElement(role);
					if (!roleType.isRole())
						continue;
					if (((IRoleType)roleType).getBaseClass() == null) {
						IType declaringTeam = roleType.getDeclaringType();
						if (declaringTeam == javaTeam)
							continue;
					}
					relevant[j++] = role;					
				}
                if (j == 0)
                	return;
                if (j < roles.length)
                	System.arraycopy(relevant, 0, roles=new IType[j], 0, j);
            }
            catch (JavaModelException ex) 
            {
                String title = Messages.format(
                		OTDTUIPlugin.getResourceString("BindingEditor.generic_error_in_dialog"), //$NON-NLS-1$
                		OTDTUIPlugin.getResourceString("BindingEditor.AddconnectorDialog.title"));   //$NON-NLS-1$
                String message = Messages.format(
                		OTDTUIPlugin.getResourceString("BindingEditor.AddTypeBindingDialog.error_retrieving_roles"), //$NON-NLS-1$
                		this._focusTeam.getFullyQualifiedName());
                
                MessageDialog.openError(getShell(), title, message);
                buttonPressed(IDialogConstants.CANCEL_ID);
            }
            _roleTypes = replaceITypesWithOTTypes(roles);
//          _roleTypes = roles.toArray(new IType[roles.size()]);             
// SH}        
        }
		
	}
    
    private static IType[] replaceITypesWithOTTypes(IType[] types)
    {
        for (int i = 0; i < types.length; i++)
        {
            IType type = types[i];
            type.exists(); // ensure it's "open"
            IType otType = OTModelManager.getOTElement(type);
            if (otType != null)
                types[i] = otType;
        }
        
        return types;
    }
	
	protected void computeResult()
	{
// CRIPPLE (3.5) migration
//		TypeNameMatch ref = getSelectedTypes()[0];
//
//		IType type= ref.getType();			
//		if (type == null)
//		{
//			// not a class file or compilation unit
//			String title= JavaUIMessages.TypeSelectionDialog_errorTitle;
//			String message= Messages.format(JavaUIMessages.TypeSelectionDialog_dialogMessage, type.getElementName());
//			MessageDialog.openError(getShell(), title, message);
//			setResult(null);
//		}
//		else
//		{
//			List<IType> result= new ArrayList<IType>(1);
//			result.add(type);
//			setResult(result);
//		}
	}
	
	protected void okPressed()
	{
        if (_rolList.isEmpty() || getResult()[0] == null)
        {
	        checkOkButton();
            String title = Messages.format(
            		OTDTUIPlugin.getResourceString("BindingEditor.generic_error_in_dialog"), //$NON-NLS-1$
            		OTDTUIPlugin.getResourceString("BindingEditor.AddconnectorDialog.title"));   //$NON-NLS-1$
	        String message = OTDTUIPlugin.getResourceString("BindingEditor.AddTypeBindingDialog.missing_class"); //$NON-NLS-1$
            MessageDialog.openError(getShell(), title, message);
        }
        else
        {
            computeResult();
            
            _baseType = (IType) super.getFirstResult();
            _baseTypeName = _baseType.getFullyQualifiedName();
            _roleType     = (IType)_rolList.getSelection()[0];
            _roleTypeName = _roleType.getFullyQualifiedName(); 
                
            setReturnCode(OK);
            close();
        }
	} 
	
	public IType getBaseType()
	{
	    return _baseType;
	}

	/**
	 * @return the selected BaseTypeName as FullyQualifiedName
	 */
	public String getBaseTypeName()
	{
		if (_baseRelativeToEnclosingBase)
			return "base."+_baseType.getElementName(); //$NON-NLS-1$
	    return _baseTypeName;
	}
	
	public IType getRoleType()
	{
	    return _roleType;
	}

	/**
	 * @return the selected RoleTypeName as FullyQualifiedName
	 */
	public String getRoleTypeName()
	{
	    return _roleTypeName;
	}
}
