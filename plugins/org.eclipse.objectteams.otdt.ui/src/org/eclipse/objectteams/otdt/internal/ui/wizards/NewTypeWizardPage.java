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
 * $Id: NewTypeWizardPage.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.refactoring.StubTypeContext;
import org.eclipse.jdt.internal.corext.refactoring.TypeContextChecker;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.Resources;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.internal.ui.dialogs.TableTextCellEditor;
import org.eclipse.jdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.CompletionContextRequestor;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.ControlContentAssistHelper;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaPackageCompletionProcessor;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaTypeCompletionProcessor;
import org.eclipse.jdt.internal.ui.util.SWTUtil;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonStatusDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jdt.ui.wizards.NewContainerWizardPage;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.ui.wizards.listeners.NewTypeWizardPageListener;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;

/**
 * The class <code>NewTypeWizardPage</code> contains controls for a
 * 'New ObjectTeams-Type WizardPage'. It is intended to serve as base class of
 * team and role creation wizards.
 * 
 * @see org.eclipse.objectteams.otdt.internal.ui.wizards.NewTeamWizardPage
 * @see org.eclipse.objectteams.otdt.internal.ui.wizards.NewRoleWizardPage
 *  
 * @author kaschja
 * @version $Id: NewTypeWizardPage.java 23435 2010-02-04 00:14:38Z stephan $
 */
public abstract class NewTypeWizardPage extends org.eclipse.jdt.ui.wizards.NewTypeWizardPage
{
	public static final int PUBLIC_INDEX    = 0; 
	public static final int DEFAULT_INDEX   = 1; 
	public static final int PRIVATE_INDEX   = 2; 
	public static final int PROTECTED_INDEX = 3;
	
	public static final int ABSTRACT_INDEX  = 0; 
	public static final int FINAL_INDEX     = 1; 
	public static final int STATIC_INDEX    = 2;

	private StringButtonStatusDialogField   _packageDialogField;	
	
	private SelectionButtonDialogField 		_enclosingTypeSelection;
	private StringButtonDialogField         _enclosingTypeDialogField;

	private SelectionButtonDialogFieldGroup _accessModifierButtons;
	private SelectionButtonDialogFieldGroup _otherModifierButtons;
	private StringButtonDialogField 		_superTypeDialogField;
	private StringDialogField 			    _typeNameDialogField;
	private ListDialogField 				_superInterfacesDialogField; 
	private SelectionButtonDialogFieldGroup _methodStubsButtons;
	private SelectionButtonDialogFieldGroup _bindingEditorButtons;
	private SelectionButtonDialogField		_inlineSelectionDialogField;
	
	private JavaPackageCompletionProcessor  _currPackageCompletionProcessor;
	private JavaTypeCompletionProcessor 	_enclosingTypeCompletionProcessor;

	private NewTypeWizardPageListener	    _listener;

	private IPackageFragment                _currentPackage;
	private IType                           _enclosingType;
	private IType                           _superType;
	private IType 							_currentType;
	
	private boolean 						_canModifyPackage;
	private boolean							_canModifyEnclosingType;
	
	private StubTypeContext 				_superClassStubTypeContext;
	private StubTypeContext 				_superInterfaceStubTypeContext;
	
//	------------------------------------------------------------------------------
//	 start of innerclass definitions
//------------------------------------------------------------------------------	
 
	public static class InterfaceWrapper {
		public String interfaceName;

		public InterfaceWrapper(String interfaceName) {
			this.interfaceName= interfaceName;
		}

		public int hashCode() {
			return interfaceName.hashCode();
		}

		public boolean equals(Object obj) {
			return obj != null && getClass().equals(obj.getClass()) && ((InterfaceWrapper) obj).interfaceName.equals(interfaceName);
		}
	}
	

	private static class InterfacesListLabelProvider extends LabelProvider {
		private Image fInterfaceImage;
		
		public InterfacesListLabelProvider() {
			fInterfaceImage= JavaPluginImages.get(JavaPluginImages.IMG_OBJS_INTERFACE);
		}
		
		public String getText(Object element) {
			return ((InterfaceWrapper) element).interfaceName;
		}
		
		public Image getImage(Object element) {
			return fInterfaceImage;
		}
	}	 
	
	public NewTypeWizardPage(String pageName) 
	{
		super(CLASS_TYPE, pageName);
		
        _listener = createPageListener();		
				
        _packageDialogField         = createPackageDialogField(_listener);		
        
        _enclosingTypeSelection= new SelectionButtonDialogField(SWT.CHECK);
        _enclosingTypeSelection.setDialogFieldListener(_listener);
        _enclosingTypeSelection.setLabelText(getEnclosingTypeLabel());
        
		_enclosingTypeDialogField   = createEnclosingTypeDialogField(_listener);
				
		_typeNameDialogField        = createTypeNameDialogField(_listener);		
		_superTypeDialogField       = createSuperClassDialogField(_listener);
		_superInterfacesDialogField = createSuperInterfacesDialogField(_listener,_listener);			
		_accessModifierButtons      = createAccessModifierButtons(_listener);		
		_otherModifierButtons       = createOtherModifierButtons(_listener);		
		_methodStubsButtons         = createMethodStubsButtons();
		_bindingEditorButtons       = createBindingEditorButtons();
		_inlineSelectionDialogField	= createInlineSelectionDialogField(_listener);
		
		_currPackageCompletionProcessor= new JavaPackageCompletionProcessor();
		_enclosingTypeCompletionProcessor= new JavaTypeCompletionProcessor(false, false);
		
		_canModifyPackage = true;
		_canModifyEnclosingType = true;
		updateEnableState();

	}
		
	protected abstract NewTypeWizardPageListener createPageListener();
		
//------------------------------------------------------------------------------	
//creation of page elements (dialog fields, selection buttons, ...)
//------------------------------------------------------------------------------
	
    protected SelectionButtonDialogFieldGroup createOtherModifierButtons(NewTypeWizardPageListener listener)
    {
		SelectionButtonDialogFieldGroup result;
        String[] buttonNames = new String[] 
		{
			/* 0 == ABSTRACT_INDEX */ NewWizardMessages.NewTypeWizardPage_modifiers_abstract,
			/* 1 == FINAL_INDEX */ NewWizardMessages.NewTypeWizardPage_modifiers_final,
			/* 2 == STATIC_INDEX*/ NewWizardMessages.NewTypeWizardPage_modifiers_static
		};
			          		
		result = new SelectionButtonDialogFieldGroup(SWT.CHECK, buttonNames, 4);
		result.setDialogFieldListener(listener);
		
		return result;
    }

    protected SelectionButtonDialogFieldGroup createAccessModifierButtons(NewTypeWizardPageListener listener)
    {
        String[] buttonNames = new String[] 
        {
        	/* 0 == PUBLIC_INDEX */ NewWizardMessages.NewTypeWizardPage_modifiers_public,
        	/* 1 == DEFAULT_INDEX */ NewWizardMessages.NewTypeWizardPage_modifiers_default,
        	/* 2 == PRIVATE_INDEX */ NewWizardMessages.NewTypeWizardPage_modifiers_private,
        	/* 3 == PROTECTED_INDEX*/ NewWizardMessages.NewTypeWizardPage_modifiers_protected
        };
        SelectionButtonDialogFieldGroup result = new SelectionButtonDialogFieldGroup(SWT.RADIO, buttonNames, 4);
		result.setDialogFieldListener(listener);
		result.setLabelText(NewWizardMessages.NewTypeWizardPage_modifiers_acc_label);
		result.setSelection(0, true);
        
        return result;
    }

    protected ListDialogField createSuperInterfacesDialogField(IListAdapter listlistener,
    														   IDialogFieldListener fieldlistener)
    {
		ListDialogField result = null;
		
        String[] buttonNames = new String[] 
        {
        	/* 0 */ NewWizardMessages.NewTypeWizardPage_interfaces_add,
        	/* 1 */ null,
        	/* 2 */ NewWizardMessages.NewTypeWizardPage_interfaces_remove
        }; 
        
		result = new ListDialogField(listlistener, buttonNames, new InterfacesListLabelProvider());		
		result.setDialogFieldListener(fieldlistener);
		
        String interfaceLabel = NewWizardMessages.NewTypeWizardPage_interfaces_class_label;
        
		result.setLabelText(interfaceLabel);
		result.setRemoveButtonIndex(2);
		
		return result;       
    }

    protected StringButtonDialogField createSuperClassDialogField(NewTypeWizardPageListener listener)
    {
		StringButtonDialogField result = new StringButtonDialogField(listener);
		result.setDialogFieldListener(listener);
		result.setLabelText(NewWizardMessages.NewTypeWizardPage_superclass_label);
		result.setButtonLabel(NewWizardMessages.NewTypeWizardPage_superclass_button);
        
        return result;
    }

    protected StringDialogField createTypeNameDialogField(NewTypeWizardPageListener listener)
    {
        StringDialogField result = new StringDialogField();
		result.setDialogFieldListener(listener);
		result.setLabelText(NewWizardMessages.NewTypeWizardPage_typename_label);
        
        return result;
    }

    protected StringButtonDialogField createEnclosingTypeDialogField(NewTypeWizardPageListener listener)
    {
        StringButtonDialogField result = new StringButtonDialogField(listener);
		result.setDialogFieldListener(listener);
		result.setButtonLabel(NewWizardMessages.NewTypeWizardPage_enclosing_button);
        
        return result;
        
    }

    protected StringButtonStatusDialogField createPackageDialogField(NewTypeWizardPageListener listener)
    {
    	StringButtonStatusDialogField result = new StringButtonStatusDialogField(listener);
		result.setDialogFieldListener(listener);
        result.setLabelText(NewWizardMessages.NewTypeWizardPage_package_label);
        result.setButtonLabel(NewWizardMessages.NewTypeWizardPage_package_button);
        result.setStatusWidthHint(NewWizardMessages.NewTypeWizardPage_default);
        
        return result;
    }

	protected SelectionButtonDialogField createInlineSelectionDialogField(NewTypeWizardPageListener listener)
	{
		SelectionButtonDialogField result = new SelectionButtonDialogField(SWT.CHECK);
		result.setDialogFieldListener(listener);
		result.setLabelText(OTNewWizardMessages.NewRoleWizardPage_inlined_checkbox_label);
		
		return result;
	}	
    
	protected abstract SelectionButtonDialogFieldGroup createMethodStubsButtons();  
	
	protected abstract SelectionButtonDialogFieldGroup createBindingEditorButtons();  
	
//------------------------------------------------------------------------------
// creation of controls - visual arrangement of page elements	
//------------------------------------------------------------------------------

    public void createControl(Composite parent) 
    {
	    initializeDialogUnits(parent);
		
	    Composite composite= new Composite(parent, SWT.NONE);
		
	    int nColumns= 4;
		
	    GridLayout layout= new GridLayout();
	    layout.numColumns= nColumns;		
	    composite.setLayout(layout);
		
	    createContainerControls(composite, nColumns);	
	    createPackageControls(composite, nColumns);	
	    createEnclosingTypeControls(composite, nColumns);
	    createInlineSelectionControls(composite, nColumns);
	    createSeparator(composite, nColumns);
	    createTypeNameControls(composite, nColumns);
	    createModifierControls(composite, nColumns);
		createInheritanceControls(composite,nColumns);	
	    createSuperInterfacesControls(composite, nColumns);
	    createMethodStubSelectionControls(composite, nColumns);
	    createBindingEditorControls(composite, nColumns);
	    createCommentControls(composite, nColumns);
	    setAddComments(StubUtility.doAddComments(getJavaProject()), true);
	    enableCommentControl(true);
		
	    setControl(composite);			
	    Dialog.applyDialogFont(composite);
    }
    
    protected void createInheritanceControls(Composite composite, int nColumns)
	{
		createSuperTypeControls(composite,nColumns);  
	}
    
	protected void createInlineSelectionControls(Composite composite, int nColumns)
    {
		DialogField.createEmptySpace(composite);
		_inlineSelectionDialogField.doFillIntoGrid(composite, nColumns-1);
    }

	/**
	 * Creates the controls for the package name field. Expects a <code>GridLayout</code> with at 
	 * least 4 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */	
	protected void createPackageControls(Composite composite, int nColumns) 
	{
		_packageDialogField.doFillIntoGrid(composite, nColumns);
		Text text= _packageDialogField.getTextControl(null);
		LayoutUtil.setWidthHint(text, getMaxFieldWidth());	
		LayoutUtil.setHorizontalGrabbing(text);
		
		ControlContentAssistHelper.createTextContentAssistant(text, _currPackageCompletionProcessor);
		TextFieldNavigationHandler.install(text);		
	}

	/**
	 * Creates the controls for the enclosing type name field. Expects a <code>GridLayout</code> with at 
	 * least 4 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createEnclosingTypeControls(Composite composite, int nColumns)
	{
		// #6891
		Composite tabGroup= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
 		tabGroup.setLayout(layout);

		_enclosingTypeSelection.doFillIntoGrid(tabGroup, 1);

		Text text= _enclosingTypeDialogField.getTextControl(composite);
		text.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result= NewWizardMessages.NewTypeWizardPage_enclosing_field_description;
			}
		});
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= getMaxFieldWidth();
		gd.horizontalSpan= 2;
		text.setLayoutData(gd);
		
		Button button= _enclosingTypeDialogField.getChangeControl(composite);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.widthHint = SWTUtil.getButtonWidthHint(button);
		button.setLayoutData(gd);
		ControlContentAssistHelper.createTextContentAssistant(text, _enclosingTypeCompletionProcessor);
		TextFieldNavigationHandler.install(text);		
	}	

	/**
	 * Creates the controls for the type name field. Expects a <code>GridLayout</code> with at 
	 * least 2 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createTypeNameControls(Composite composite, int nColumns) 
	{
		_typeNameDialogField.doFillIntoGrid(composite, nColumns - 1);
		DialogField.createEmptySpace(composite);
		
		LayoutUtil.setWidthHint(_typeNameDialogField.getTextControl(null), getMaxFieldWidth());
	}

	/**
	 * Creates the controls for the modifiers radio/ceckbox buttons. Expects a 
	 * <code>GridLayout</code> with at least 3 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createModifierControls(Composite composite, int nColumns) 
	{
		LayoutUtil.setHorizontalSpan(_accessModifierButtons.getLabelControl(composite), 1);
		
		Control control= _accessModifierButtons.getSelectionButtonsGroup(composite);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= nColumns - 2;
		control.setLayoutData(gd);
		
		DialogField.createEmptySpace(composite);
		
		DialogField.createEmptySpace(composite);
		
		control= _otherModifierButtons.getSelectionButtonsGroup(composite);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= nColumns - 2;
		control.setLayoutData(gd);		

		DialogField.createEmptySpace(composite);
	}

	/**
	 * Creates the controls for the superclass name field. Expects a <code>GridLayout</code> 
	 * with at least 3 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createSuperTypeControls(Composite composite, int nColumns) 
	{
		_superTypeDialogField.doFillIntoGrid(composite, nColumns);
		Text text= _superTypeDialogField.getTextControl(null);
		LayoutUtil.setWidthHint(text, getMaxFieldWidth());
		
		JavaTypeCompletionProcessor superClassCompletionProcessor= new JavaTypeCompletionProcessor(false, false);
		superClassCompletionProcessor.setCompletionContextRequestor(new CompletionContextRequestor() {
			public StubTypeContext getStubTypeContext() {
				return getSuperClassStubTypeContext();
			}
		});

		ControlContentAssistHelper.createTextContentAssistant(text, superClassCompletionProcessor);
		TextFieldNavigationHandler.install(text);
	}
	
	StubTypeContext getSuperClassStubTypeContext() {
		if (_superClassStubTypeContext == null) {
			String typeName;
			if (_currentType != null) {
				typeName= getTypeName();
			} else {
				typeName= JavaTypeCompletionProcessor.DUMMY_CLASS_NAME;
			}
			_superClassStubTypeContext = TypeContextChecker.createSuperClassStubTypeContext(typeName, getEnclosingType(), getPackageFragment());
		}
		return _superClassStubTypeContext;
	}
	
	/**
	 * Hook method that gets called when the type name has changed. The method validates the 
	 * type name and returns the status of the validation.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 * 
	 * @return the status of the validation
	 */
	protected IStatus typeNameChanged() {
		super.typeNameChanged();
		
		StatusInfo status= new StatusInfo();
		_currentType= null;
		String typeNameWithParameters= getTypeName();
		// must not be empty
		if (typeNameWithParameters.length() == 0) {
			status.setError(NewWizardMessages.NewTypeWizardPage_error_EnterTypeName); 
			return status;
		}
		
		String typeName= getTypeNameWithoutParameters();
		if (typeName.indexOf('.') != -1) {
			status.setError(NewWizardMessages.NewTypeWizardPage_error_QualifiedName); 
			return status;
		}
		IStatus val= validateJavaTypeName(typeName, getJavaProject());
		if (val.getSeverity() == IStatus.ERROR) {
			status.setError(Messages.format(NewWizardMessages.NewTypeWizardPage_error_InvalidTypeName, val.getMessage())); 
			return status;
		} else if (val.getSeverity() == IStatus.WARNING) {
			status.setWarning(Messages.format(NewWizardMessages.NewTypeWizardPage_warning_TypeNameDiscouraged, val.getMessage())); 
			// continue checking
		}		

		// must not exist
		if (!isEnclosingTypeSelected()) {
			IPackageFragment pack= getPackageFragment();
			if (pack != null) {
				ICompilationUnit cu= pack.getCompilationUnit(getCompilationUnitName(typeName));
				_currentType= cu.getType(typeName);
				IResource resource= cu.getResource();

				if (resource.exists()) {
					status.setError(NewWizardMessages.NewTypeWizardPage_error_TypeNameExists); 
					return status;
				}
				URI location= resource.getLocationURI();
				if (location != null) {
					try {
						IFileStore store= EFS.getStore(location);
						if (store.fetchInfo().exists()) {
							status.setError(NewWizardMessages.NewTypeWizardPage_error_TypeNameExistsDifferentCase); 
							return status;
						}
					} catch (CoreException e) {
						status.setError(Messages.format(
							NewWizardMessages.NewTypeWizardPage_error_uri_location_unkown, 
							Resources.getLocationString(resource)));
					}
				}
			}
		} else {
			IType type= getEnclosingType();
			if (type != null) {
				_currentType= type.getType(typeName);
				if (_currentType.exists()) {
					status.setError(NewWizardMessages.NewTypeWizardPage_error_TypeNameExists); 
					return status;
				}
			}
		}
		
		if (typeNameWithParameters != typeName) {
			IPackageFragmentRoot root= getPackageFragmentRoot();
			if (root != null) {
				if (!JavaModelUtil.is50OrHigher(root.getJavaProject())) {
					status.setError(NewWizardMessages.NewTypeWizardPage_error_TypeParameters); 
					return status;
				}
				String typeDeclaration= "class " + typeNameWithParameters + " {}"; //$NON-NLS-1$//$NON-NLS-2$
				ASTParser parser= ASTParser.newParser(AST.JLS3);
				parser.setSource(typeDeclaration.toCharArray());
				parser.setProject(root.getJavaProject());
				CompilationUnit compilationUnit= (CompilationUnit) parser.createAST(null);
				IProblem[] problems= compilationUnit.getProblems();
				if (problems.length > 0) {
					status.setError(Messages.format(NewWizardMessages.NewTypeWizardPage_error_InvalidTypeName, problems[0].getMessage())); 
					return status;
				}
			}
		}
		return status;
	}
	
	private String getTypeNameWithoutParameters() {
		String typeNameWithParameters= getTypeName();
		int angleBracketOffset= typeNameWithParameters.indexOf('<');
		if (angleBracketOffset == -1) {
			return typeNameWithParameters;
		} else {
			return typeNameWithParameters.substring(0, angleBracketOffset);
		}
	}


	/**
	 * Creates the controls for the superclass name field. Expects a <code>GridLayout</code> with 
	 * at least 3 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */			
//	protected void createSuperInterfacesControls(Composite composite, int nColumns) 
//	{
//		_superInterfacesDialogField.doFillIntoGrid(composite, nColumns);
//		GridData gd = (GridData)_superInterfacesDialogField.getListControl(null).getLayoutData();
//		gd.heightHint = convertHeightInCharsToPixels(3);
//		gd.grabExcessVerticalSpace = false;
//		gd.widthHint = getMaxFieldWidth();
//	}
	
	/**
	 * Creates the controls for the superclass name field. Expects a <code>GridLayout</code> with 
	 * at least 3 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */			
	protected void createSuperInterfacesControls(Composite composite, int nColumns) {
		final String INTERFACE= "interface"; //$NON-NLS-1$
		_superInterfacesDialogField.doFillIntoGrid(composite, nColumns);
		final TableViewer tableViewer= _superInterfacesDialogField.getTableViewer();
		tableViewer.setColumnProperties(new String[] {INTERFACE});
		
		TableTextCellEditor cellEditor= new TableTextCellEditor(tableViewer, 0) {
		    protected void doSetFocus() {
		        if (text != null) {
		            text.setFocus();
		            text.setSelection(text.getText().length());
		            checkSelection();
		            checkDeleteable();
		            checkSelectable();
		        }
		    }
		};
		JavaTypeCompletionProcessor superInterfaceCompletionProcessor= new JavaTypeCompletionProcessor(false, false);
		superInterfaceCompletionProcessor.setCompletionContextRequestor(new CompletionContextRequestor() {
			public StubTypeContext getStubTypeContext() {
				return getSuperInterfacesStubTypeContext();
			}
		});
		SubjectControlContentAssistant contentAssistant= ControlContentAssistHelper.createJavaContentAssistant(superInterfaceCompletionProcessor);
		Text cellEditorText= cellEditor.getText();
		ContentAssistHandler.createHandlerForText(cellEditorText, contentAssistant);
		TextFieldNavigationHandler.install(cellEditorText);
		cellEditor.setContentAssistant(contentAssistant);
		
		tableViewer.setCellEditors(new CellEditor[] { cellEditor });
		tableViewer.setCellModifier(new ICellModifier() {
			public void modify(Object element, String property, Object value) {
				if (element instanceof Item)
					element = ((Item) element).getData();
				
				((InterfaceWrapper) element).interfaceName= (String) value;
				_superInterfacesDialogField.elementChanged(element);
			}
			public Object getValue(Object element, String property) {
				return ((InterfaceWrapper) element).interfaceName;
			}
			public boolean canModify(Object element, String property) {
				return true;
			}
		});
		tableViewer.getTable().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.F2 && event.stateMask == 0) {
					ISelection selection= tableViewer.getSelection();
					if (! (selection instanceof IStructuredSelection))
						return;
					IStructuredSelection structuredSelection= (IStructuredSelection) selection;
					tableViewer.editElement(structuredSelection.getFirstElement(), 0);
				} 
			}
		});
		GridData gd= (GridData) _superInterfacesDialogField.getListControl(null).getLayoutData();

		gd.heightHint= convertHeightInCharsToPixels(3);

		gd.grabExcessVerticalSpace= false;
		gd.widthHint= getMaxFieldWidth();
	}
	
	private StubTypeContext getSuperInterfacesStubTypeContext() {
		if (_superInterfaceStubTypeContext == null) {
			String typeName;
			if (_currentType != null) {
				typeName= getTypeName();
			} else {
				typeName= JavaTypeCompletionProcessor.DUMMY_CLASS_NAME;
			}
			_superInterfaceStubTypeContext= TypeContextChecker.createSuperInterfaceStubTypeContext(typeName, getEnclosingType(), getPackageFragment());
		}
		return _superInterfaceStubTypeContext;
	}
	
	protected void createMethodStubSelectionControls(Composite composite, int nColumns) 
	{
		Control labelControl = _methodStubsButtons.getLabelControl(composite);
		LayoutUtil.setHorizontalSpan(labelControl, nColumns);
		
		DialogField.createEmptySpace(composite);
		
		Control buttonGroup = _methodStubsButtons.getSelectionButtonsGroup(composite);
		LayoutUtil.setHorizontalSpan(buttonGroup, nColumns - 1);	
	}

	protected void createBindingEditorControls(Composite composite, int nColumns) 
	{
	    if (_bindingEditorButtons != null)
	    {
	        DialogField.createEmptySpace(composite);
	        Control labelControl = _bindingEditorButtons.getLabelControl(composite);
	        LayoutUtil.setHorizontalSpan(labelControl, nColumns);
	        
	        DialogField.createEmptySpace(composite);
	        
	        Control buttonGroup = _bindingEditorButtons.getSelectionButtonsGroup(composite);
	        LayoutUtil.setHorizontalSpan(buttonGroup, nColumns - 1);
	    }
	}
	
	/**
	 * Creates the controls for the baseclass name field. Expects a <code>GridLayout</code> 
	 * with at least 3 columns.
	 * This method gets called by createControls.
	 * It is intended to be overridden by the subclass
	 * org.eclipse.objectteams.otdt.internal.ui.wizards.NewRoleWizardPage. 
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createBaseClassControls(Composite composite, int nColumns){}
	
//------------------------------------------------------------------------------
//	 start of init and set methods	
//------------------------------------------------------------------------------	
 
    /**
     * The wizard owning this page is responsible for calling this method with the
     * current selection. The selection is used to initialize the fields of the wizard 
     * page.
     * 
     * @param selection used to initialize the fields
     */
    public void init(IStructuredSelection selection) 
    {
        IJavaElement jelem = getInitialJavaElement(selection);
        initContainerPage(jelem);
        initTypePage(jelem);
    }

	/**
	 * Initializes all fields provided by the page with a given selection.
	 * 
	 * @param elem the selection used to intialize this page or <code>
	 * null</code> if no selection was available
	 */
	protected void initTypePage(IJavaElement elem) 
	{	
		getInlineSelectionDialogField().setEnabled(false);
		
		initAccessModifierButtons();
		initOtherModifierButtons();
		initMethodStubButtons();
		initBindingEditorButtons();
		
		IJavaProject project= null;
		if (elem != null)
		{
			project= elem.getJavaProject();	
			initPackageAndEnclosingType(elem);
		}			
		setTypeName(""); //$NON-NLS-1$
		setSuperInterfaces(new ArrayList(5));
		setAddComments(StubUtility.doAddComments(project), true); // from project or workspace
	}
	

	/**
	 * initializes the package and enclosing type dialog fields
	 * depending on the given initial selected IJavaElement
	 * (that is the IJavaElement which was selected in the Package Explorer,
	 *  when the request to open the wizard occured)
	 */
	protected void initPackageAndEnclosingType(IJavaElement initialSelectedElem)
	{
		IType potentialEnclosingType = null;
		IType typeInCU = (IType) initialSelectedElem.getAncestor(IJavaElement.TYPE);
		
		if (typeInCU != null) 
		{
			if (typeInCU.getCompilationUnit() != null) 
			{
				potentialEnclosingType = typeInCU;
			}
		} 
		else 
		{
			ICompilationUnit cu = (ICompilationUnit) initialSelectedElem.getAncestor(IJavaElement.COMPILATION_UNIT);
			if (cu != null) 
			{
				potentialEnclosingType = cu.findPrimaryType();
			}
		}		
		
		//default case
		IPackageFragment packageFragment = (IPackageFragment) initialSelectedElem.getAncestor(IJavaElement.PACKAGE_FRAGMENT);			
		String           packName        = (packageFragment == null) 
		                                       ? ""  //$NON-NLS-1$
		                                       : packageFragment.getElementName();
		setPackageFragmentName(packName); 
		setEnclosingTypeName(""); //$NON-NLS-1$
			
		if (potentialEnclosingType != null)
		{
			if (OTModelManager.hasOTElementFor(potentialEnclosingType))
			{
				IOTType potentialEnclosingOTType = OTModelManager.getOTElement(potentialEnclosingType);
				
				boolean hasChanges = false;
				if (potentialEnclosingOTType.isTeam())
				{
					handleTeamSelected(potentialEnclosingOTType);
					hasChanges = true;
				}
				else //if potentialEnclosingOTType.isRole()
				{
					handleRoleSelected(potentialEnclosingOTType);
					hasChanges = true;
				}

				if (hasChanges)
				{
				}
			}
			else try
			{
				if (potentialEnclosingType.isClass())
				{
					handleClassSelected(potentialEnclosingType);
				}
			}
			catch (JavaModelException ex)
			{
				OTDTUIPlugin.getExceptionHandler().logCoreException(null,ex);
			}
		}            
	}    

	/**
	 * sets the enclosing type to be the given team class and
	 * the package fragment to be the package fragment of that team
	 * @param potentialEnclosingTeam - isTeam must be true
	 */
	private void handleTeamSelected(IOTType potentialEnclosingTeam)
	{
		IType            enclosingTeam   = null;
		IPackageFragment packageFragment = null;	
		
		if (potentialEnclosingTeam != null)
		{
//{OTModelUpdate		    
			enclosingTeam   = (IType) potentialEnclosingTeam.getCorrespondingJavaElement();
//haebor}			
			packageFragment = enclosingTeam.getPackageFragment();			
		}    	

		String enclosingTeamName = (enclosingTeam == null) ? "" : enclosingTeam.getFullyQualifiedName('.'); //$NON-NLS-1$
		setEnclosingTypeName(enclosingTeamName);

		String packName = (packageFragment == null) ? "" : packageFragment.getElementName(); //$NON-NLS-1$
		setPackageFragmentName(packName); 		    	
	}
    
	/**
	 * sets the enclosing type to be the enclosing team of the given role class and
	 * the package fragment to be the package fragment of that team
	 * @param potentialEnclosingRole - isRole() must be true
	 */    
	private void handleRoleSelected(IOTType potentialEnclosingRole)
	{	
		IType            enclosingTeam   = null;
		IPackageFragment packageFragment = null;	
		
		if (potentialEnclosingRole != null) {
			if (potentialEnclosingRole.isRole()) 
			{
				IJavaElement parent = potentialEnclosingRole.getParent();
				if (parent instanceof IOTType) {
					enclosingTeam = (IType)((IOTType)parent).getCorrespondingJavaElement();
					packageFragment = enclosingTeam.getPackageFragment();
				}
			} 
			if (packageFragment == null) {
				packageFragment = potentialEnclosingRole.getPackageFragment();
			}
		}
		
		String enclosingTeamName = (enclosingTeam == null) ? "" : enclosingTeam.getFullyQualifiedName('.'); //$NON-NLS-1$
		setEnclosingTypeName(enclosingTeamName);
		
		String packName = (packageFragment == null) ? "" : packageFragment.getElementName(); //$NON-NLS-1$
		setPackageFragmentName(packName);         		
	}	
 
	/**
	 * sets the enclosing type to be null and
	 * the package fragment to be the package fragment of the given class
	 */  
	private void handleClassSelected(IType potentialEnclosingType)
	{
		setEnclosingTypeName(""); //$NON-NLS-1$
		
		IPackageFragment pack     = potentialEnclosingType.getPackageFragment();
		String           packName = (pack == null) ? "" : pack.getElementName(); //$NON-NLS-1$
		setPackageFragmentName(packName);		    	
	}
		
	protected abstract void initAccessModifierButtons();
	
	protected void initOtherModifierButtons()
	{
		_otherModifierButtons.enableSelectionButton(STATIC_INDEX, false);
		_otherModifierButtons.enableSelectionButton(FINAL_INDEX, true);		
	}
	
	/**
	 * Sets the selection state of the method stub checkboxes.
	 */
	protected abstract void initMethodStubButtons(); 	

	/**
	 * Sets the selection state of the bindingeditor checkbox.
	 */
	protected abstract void initBindingEditorButtons(); 	

	/**
	 * Sets the enclosing type. The method updates the underlying model 
	 * and the text of the control.
	 * 
	 * @param type the enclosing type
	 * @param canBeModified if <code>true</code> the enclosing type field is
	 * editable; otherwise it is read-only.
	 */	
	public void setEnclosingType(IType type, boolean canBeModified) {
		super.setEnclosingType(type, canBeModified);
		
		_enclosingType= type;
		_canModifyEnclosingType= canBeModified;
		updateEnableState();
	}

	public void setEnclosingTypeName(String qualifiedName) 
	{
		getEnclosingTypeDialogField().setText(qualifiedName);
	}
	
	/**
	 * Sets the package fragment to the given value. The method updates the model. 
	 * It does NOT update the text of the control the text of the control.
	 * 
	 * @param pack the package fragment to be set
	 */
	public void setPackageFragment(IPackageFragment pack, boolean canBeModified) {
		_currentPackage = pack;
		_canModifyPackage= canBeModified;
		updateEnableState();
	}
	
	public void setPackageFragmentName(String packageName)
	{
	    if (getPackageDialogField() != null)
	    {
	        getPackageDialogField().setText(packageName);
	    }
	}
	
	/**
	 * Sets the type name input field's text to the given value. Method doesn't update
	 * the model.
	 * 
	 * @param name the new type name
	 */	
	public void setTypeName(String name)
	{
		_typeNameDialogField.setText(name);
		_typeNameDialogField.setEnabled(true);
	}	
	
	/**
	 * Sets the super interfaces.
	 * 
	 * @param interfacesNames a list of super interface. The method requires that
	 * the list's elements are of type <code>String</code>
	 */	
	public void setSuperInterfaces(List interfacesNames)
	{
		_superInterfacesDialogField.setElements(interfacesNames);
		_superInterfacesDialogField.setEnabled(true);
	}

	public void setSuperType(IType type)
	{
		_superType = type;
	}
	
	public void setSuperTypeName(String name) 
	{
		getSuperTypeDialogField().setText(name);
		getSuperTypeDialogField().setEnabled(true);
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setFocus();
			IStatus status = StatusUtil.getMostSevere(((NewTypeWizardPageListener)this._listener).getRelevantStates(true/*ignoreFirstField*/));
			if (!status.isOK())
				StatusUtil.applyToStatusLine(this, status);
		}
	}	
	
	/**
	 * Sets the focus on the type name input field.
	 */		
	protected void setFocus() 
	{
		_typeNameDialogField.setFocus();
	}	


//------------------------------------------------------------------------------
//	start of get methods
//-----------------------------------------------------------------------------	
 
    public SelectionButtonDialogFieldGroup getAccessModifierButtons()
	{
		return _accessModifierButtons;
	}
	
	public SelectionButtonDialogFieldGroup getOtherModifierButtons()
	{
		return _otherModifierButtons;
	}
	
	public SelectionButtonDialogFieldGroup getMethodStubsButtons()
	{
		return _methodStubsButtons;
	}
	
	public abstract boolean isCreateMainSelected();
	
	public abstract boolean isCreateInheritedSelected();
	
	public abstract boolean isCreateConstructorsSelected();
	

	public SelectionButtonDialogFieldGroup getBindingEditorButtons()
	{
		return _bindingEditorButtons;
	}

	public abstract boolean isOpenBindingEditorSelected();
	
	public NewTypeWizardPageListener getListener()
	{
		return _listener;
	}

	public StringButtonStatusDialogField getPackageDialogField()
	{
		return _packageDialogField;
	}
	
	public StringDialogField getTypeNameDialogField()
	{
		return _typeNameDialogField;
	}
	
	public StringButtonDialogField getEnclosingTypeDialogField()
	{
		return _enclosingTypeDialogField;
	}
	
	public StringButtonDialogField getSuperTypeDialogField()
	{
		return _superTypeDialogField;
	}	

	public SelectionButtonDialogField getInlineSelectionDialogField()
	{
		return _inlineSelectionDialogField;
	}	
	
	public ListDialogField getSuperInterfacesDialogField()
	{
		return _superInterfacesDialogField;
	}	
	
	public SelectionButtonDialogField getEnclosingTypeSelectionField() {
		return _enclosingTypeSelection;
	}
		
	public String getTypeName()
	{
		return getTypeNameDialogField().getText();
	}
	
	public IPackageFragment getPackageFragment()
	{
		return _currentPackage;
	}
	
	public String getPackageName()
	{
	    if (_packageDialogField != null)
	    {
	        return _packageDialogField.getText();
	    }
	    else
	    {
	        return ""; //$NON-NLS-1$
	    }
	}		

	public IType getEnclosingType()
	{
		return _enclosingType;
	}

	public String getEnclosingTypeName()
	{
		return getEnclosingTypeDialogField().getText();
	}

	public boolean isInlineTypeSelected()
	{
		return getInlineSelectionDialogField().isSelected();	
	}
		
	public IType getSuperType()
	{
		return _superType;
	}
		
	public String getSuperTypeName()
	{
		return getSuperTypeDialogField().getText();
	}
	
	/**
	 * Returns the chosen super interfaces.
	 * 
	 * @return a list of chosen super interfaces. The list's elements
	 * are of type <code>String</code>
	 */
	@SuppressWarnings("unchecked") // ListDialogField uses raw List
	public List getSuperInterfaces() {
		List interfaces= _superInterfacesDialogField.getElements();
		ArrayList result= new ArrayList(interfaces.size());
		for (Iterator iter= interfaces.iterator(); iter.hasNext();) {
			InterfaceWrapper wrapper= (InterfaceWrapper) iter.next();
			result.add(wrapper.interfaceName);
		}
		return result;
	}
	/**
	 * Hook method that gets called when the list of super interface has changed. The method 
	 * validates the super interfaces and returns the status of the validation.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 * 
	 * @return the status of the validation
	 */
	protected IStatus superInterfacesChanged() {
		StatusInfo status= new StatusInfo();
		
		IPackageFragmentRoot root= getPackageFragmentRoot();
		_superInterfacesDialogField.enableButton(0, root != null);
						
		if (root != null) {
			List elements= _superInterfacesDialogField.getElements();
			int nElements= elements.size();
			for (int i= 0; i < nElements; i++) {
				String intfname= ((InterfaceWrapper) elements.get(i)).interfaceName;
				Type type= TypeContextChecker.parseSuperInterface(intfname);
				if (type == null) {
					status.setError(Messages.format(NewWizardMessages.NewTypeWizardPage_error_InvalidSuperInterfaceName, intfname)); 
					return status;
				}
				if (type instanceof ParameterizedType && ! JavaModelUtil.is50OrHigher(root.getJavaProject())) {
					status.setError(Messages.format(NewWizardMessages.NewTypeWizardPage_error_SuperInterfaceNotParameterized, intfname)); 
					return status;
				}
			}				
		}
		return status;
	}

	/**
	 * Sets the super interfaces.
	 * 
	 * @param interfacesNames a list of super interface. The method requires that
	 * the list's elements are of type <code>String</code>
	 * @param canBeModified if <code>true</code> the super interface field is
	 * editable; otherwise it is read-only.
	 */	
	@SuppressWarnings("unchecked") // we are called with a raw List
	public void setSuperInterfaces(List interfacesNames, boolean canBeModified) {
		ArrayList interfaces= new ArrayList(interfacesNames.size());
		for (Iterator iter= interfacesNames.iterator(); iter.hasNext();) {
			interfaces.add(new InterfaceWrapper((String) iter.next()));
		}
		_superInterfacesDialogField.setElements(interfaces);
		_superInterfacesDialogField.setEnabled(canBeModified);
	}
	
	/**
	 * Adds a super interface to the end of the list and selects it if it is not in the list yet.
	 * 
	 * @param superInterface the fully qualified type name of the interface.
	 * @return returns <code>true</code>if the interfaces has been added, <code>false</code>
	 * if the interface already is in the list.
	 * @since 3.2
	 */
	public boolean addSuperInterface(String superInterface) {
		return _superInterfacesDialogField.addElement(new InterfaceWrapper(superInterface));
	}

										
	/**
	 * Returns the selected modifiers.
	 * 
	 * @return the selected modifiers
	 * @see org.eclipse.jdt.core.Flags 
	 */	
	public int getModifiers() 
	{
		int mdf= 0;
		if (getAccessModifierButtons().isSelected(PUBLIC_INDEX)) 
		{
			mdf+= Flags.AccPublic;
		} else if (getAccessModifierButtons().isSelected(PRIVATE_INDEX)) 
		{
			mdf+= Flags.AccPrivate;
		} else if (getAccessModifierButtons().isSelected(PROTECTED_INDEX)) 
		{	
			mdf+= Flags.AccProtected;
		}
		if (getOtherModifierButtons().isSelected(ABSTRACT_INDEX) && (STATIC_INDEX != 0)) 
		{	
			mdf+= Flags.AccAbstract;
		}
		if (getOtherModifierButtons().isSelected(FINAL_INDEX)) 
		{	
			mdf+= Flags.AccFinal;
		}
		if (getOtherModifierButtons().isSelected(STATIC_INDEX)) 
		{	
			mdf+= Flags.AccStatic;
		}
		return mdf;
	}	

	public IStatus getContainerStatus()
	{
		//inherited from NewContainerWizardPage
		return fContainerStatus;
	}
		
	/**
	 * extended visibility in order to enable calls from external listeners
	 * @see NewContainerWizardPage.updateStatus
	 */
	public void updateStatus(IStatus[] status)
	{
		super.updateStatus(status);
	}
	
	/**
	 * workaround
	 * the containerDialogField is defined in a superclass and declared private
	 * 
	 * informs the listener that the container dialogfield has changed
	 */
	protected void handleFieldChanged(String fieldName) 
	{
		super.handleFieldChanged(fieldName);
		if (fieldName == CONTAINER) 
		{
			getListener().handleContainerChanged();
		}
	}	
	
	/** A bridge to the protected method typeNameChanged() */
	public IStatus publicTypeNameChanged() {
		return typeNameChanged();
	}
		
	/**
	 * Returns the selection state of the enclosing type checkbox.
	 * 
	 * @return the selection state of the enclosing type checkbox
	 */
	public boolean isEnclosingTypeSelected() {
		if(_enclosingTypeSelection == null)
			return super.isEnclosingTypeSelected();
		else
			return _enclosingTypeSelection.isSelected();
	}

	/**
	 * Sets the enclosing type checkbox's selection state.
	 * 
	 * @param isSelected the checkbox's selection state
	 * @param canBeModified if <code>true</code> the enclosing type checkbox is
	 * modifiable; otherwise it is read-only.
	 */
	public void setEnclosingTypeSelection(boolean isSelected, boolean canBeModified) {
		super.setEnclosingTypeSelection(isSelected, canBeModified);
		_enclosingTypeSelection.setSelection(isSelected);
		_enclosingTypeSelection.setEnabled(canBeModified);
		updateEnableState();
	}
	
	/*
	 * Updates the enable state of buttons related to the enclosing type selection checkbox.
	 */
	public void updateEnableState() {
		boolean enclosing= isEnclosingTypeSelected();
		if(_packageDialogField != null)
			_packageDialogField.setEnabled(_canModifyPackage && !enclosing);
		_enclosingTypeDialogField.setEnabled(_canModifyEnclosingType && enclosing);
	}	

	/*
	 * @see org.eclipse.jdt.ui.wizards.NewContainerWizardPage#containerChanged()
	 */
	protected IStatus containerChanged() {
		IStatus status= super.containerChanged();
		
		_currPackageCompletionProcessor.setPackageFragmentRoot(getPackageFragmentRoot());
		if (getPackageFragmentRoot() != null) {
			_enclosingTypeCompletionProcessor.setPackageFragment(getPackageFragmentRoot().getPackageFragment("")); //$NON-NLS-1$
		}
		return status;
	}
	
	/**
	 * Returns the label that is used for the enclosing type input field.
	 * 
	 * @return the label that is used for the enclosing type input field.
	 * @since 3.2
	 */
	protected String getEnclosingTypeLabel() {
		return NewWizardMessages.NewTypeWizardPage_enclosing_selection_label;
	}
	
	IType getCurrentType() {
		return _currentType;
	}
	
	@Override
	public IType chooseEnclosingType() {
		return super.chooseEnclosingType();
	}

	public static IStatus validateJavaTypeName(String typeName, IJavaProject javaProject) {
		String sourceLevel     = CompilerOptions.VERSION_1_5;
		String complianceLevel = CompilerOptions.VERSION_1_5;
		if (javaProject != null) {
			sourceLevel = javaProject.getOption(CompilerOptions.OPTION_Source, true);
			complianceLevel = javaProject.getOption(CompilerOptions.OPTION_Compliance, true);
		}
		return JavaConventions.validateJavaTypeName(typeName, sourceLevel, complianceLevel);
	}
	public static IStatus validatePackageName(String packageName, IJavaProject javaProject) {
		String sourceLevel     = CompilerOptions.VERSION_1_5;
		String complianceLevel = CompilerOptions.VERSION_1_5;
		if (javaProject != null) {
			sourceLevel = javaProject.getOption(CompilerOptions.OPTION_Source, true);
			complianceLevel = javaProject.getOption(CompilerOptions.OPTION_Compliance, true);
		}
		return JavaConventions.validatePackageName(packageName, sourceLevel, complianceLevel);
	}
}
