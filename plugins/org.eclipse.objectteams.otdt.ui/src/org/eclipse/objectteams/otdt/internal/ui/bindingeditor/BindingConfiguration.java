/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BindingConfiguration.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.bindingeditor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractMethodMappingDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodMappingElement;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTNodeCreator;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.TreeHierarchyLayoutProblemsDecorator;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

/**
 * Select role-Method, callout or callin-kind and base-Method for creation of Callin-/CalloutMappingDeclaration.
 * 
 * @author jwloka
 */
public class BindingConfiguration extends Composite
{
	private static final int DESELECT_DISABLE_BUTTONS = 1;
	private static final int SELECT_BUTTONS = 0;
	private static final String OT_GENERATED_INDICATOR = "_OT$"; //$NON-NLS-1$
	private static final String FAKED_METHOD_NAME = '\''+Messages.BindingConfiguration_new_method_label;
	private static final Object[] EMPTY_LIST = new Object[0];
	private static final int OT_CALLOUT = 0x1000000;
	private static final int OT_CALLOUT_OVERRIDE = 0x2000000;
	
	private IMethod[] _roleMethods;
	private IMember[] _baseMethods;
    private IMethod _fakedMeth;
	
	final TableViewer _roleMethListViewer;
	final TableViewer _baseMethListViewer;
	final RadioButtonComposite _methMapBtnComp;
	private Button _calloutBtn; 
	private Button _calloutOverrideBtn; 
	private Button _callinBeforeBtn;
	private Button _callinReplaceBtn; 
	private Button _callinAfterBtn;
	private RoleTypeDeclaration _selectedRole;
	private CalloutMappingDeclaration _selectedCalloutDecl;
	private CalloutMappingDeclaration _calloutMapping;
	private CallinMappingDeclaration _selectedCallinDecl;
	private CallinMappingDeclaration _callinMapping;
	private BindingEditor _bindingEditor;
	
	private Button _applyBtn; 
	private boolean _newCallout;
	private List _parameterMappings;
	private IType _curTeam;
	
	class BaseMethodContentProvider implements IStructuredContentProvider 
	{
		public Object[] getElements(Object inputElement)
		{
			return (_baseMethods != null) ? getFilteredMethods() : EMPTY_LIST;    
		}
		
		public void dispose() {}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		
		private Object[] getFilteredMethods() 
		{
			List<IMember> result = new ArrayList<IMember>();
			for (int idx = 0; idx < _baseMethods.length; idx++)
			{
				IMember curElem = _baseMethods[idx];
				try {
					if (   !curElem.getElementName().startsWith(OT_GENERATED_INDICATOR)
						&& !Flags.isSynthetic(curElem.getFlags()))
					{
						result.add(curElem);
					}
				} catch (JavaModelException jme) {
					// nop
				}
			}
			
			return result.toArray();
		}
	}
	
	class RoleMethodContentProvider implements IStructuredContentProvider 
	{
		public Object[] getElements(Object inputElement)
		{
			return getFakedMethodList();
		}
		
		private Object[] getFakedMethodList() 
		{
			if (_curTeam == null) 
			{
				return EMPTY_LIST;
			}
			
			_fakedMeth = _curTeam.getMethod(FAKED_METHOD_NAME, new String[0] );
			
			if (_roleMethods == null) 
			{
				return new IMethod[] { _fakedMeth };
			}
			else 
			{
				List<IMethod> result = new ArrayList<IMethod>();
				for (int idx = 0; idx < _roleMethods.length; idx++)
				{
					IMethod curElem = _roleMethods[idx];
					if (!curElem.getElementName().startsWith(OT_GENERATED_INDICATOR))
					{
						result.add(curElem);
					}
				}
				result.add(_fakedMeth);
				
				return result.toArray();
			}
		}
		
		public void dispose() {}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}
	
	
	public BindingConfiguration(Composite parent, int style)
	{
		super(parent, style);
		setLayout(new FormLayout());
		
		_bindingEditor = (BindingEditor)parent.getParent();
		
		JavaUILabelProvider labelProvider = new JavaUILabelProvider();
		labelProvider.addLabelDecorator(new TreeHierarchyLayoutProblemsDecorator());
		labelProvider.setTextFlags(JavaElementLabels.M_APP_RETURNTYPE | JavaElementLabels.M_PARAMETER_TYPES | JavaElementLabels.M_PARAMETER_NAMES);
		
		final Composite composite = new Composite(this, SWT.NONE);
		final FormData formData_10 = new FormData();
		formData_10.bottom = new FormAttachment(100, -5);
		formData_10.right = new FormAttachment(100, -5);
		formData_10.top = new FormAttachment(100, -50);
		formData_10.left = new FormAttachment(0, 5);
		composite.setLayoutData(formData_10);
		composite.setLayout(new FormLayout());
		
		_applyBtn = new Button(composite, SWT.NONE);
		final FormData formData_11 = new FormData();
		formData_11.bottom = new FormAttachment(100, -5);
		formData_11.right = new FormAttachment(100, -5);
		_applyBtn.setLayoutData(formData_11);
		_applyBtn.setText(Messages.BindingConfiguration_apply_button);
		_applyBtn.setEnabled(false);
		addApplyButtonListener(_applyBtn);
		
		// method mapping button group
		_methMapBtnComp = new RadioButtonComposite(this, SWT.NONE);
		final FormData formData_1 = new FormData();
		formData_1.right = new FormAttachment(60, -5);
		formData_1.bottom = new FormAttachment(composite, 5, SWT.TOP);
		formData_1.top = new FormAttachment(0, 8);
		formData_1.left = new FormAttachment(40, 5);
		_methMapBtnComp.setLayoutData(formData_1);
		_methMapBtnComp.setLayout(new FormLayout());
		
		_calloutBtn = new Button(_methMapBtnComp, SWT.TOGGLE);
		addButtonListener(_calloutBtn);
		final FormData formData_3 = new FormData();
		formData_3.right = new FormAttachment(100, -5);
		formData_3.top = new FormAttachment(0, 5);
		formData_3.left = new FormAttachment(0, 5);
		_calloutBtn.setLayoutData(formData_3);
		_calloutBtn.setText("->"); //$NON-NLS-1$
		
		_calloutOverrideBtn = new Button(_methMapBtnComp, SWT.TOGGLE);
		addButtonListener( _calloutOverrideBtn);
		final FormData formData_4 = new FormData();
		formData_4.right = new FormAttachment(100, -5);
		formData_4.top = new FormAttachment(_calloutBtn, 5, SWT.BOTTOM);
		formData_4.left = new FormAttachment(0, 5);
		_calloutOverrideBtn.setLayoutData(formData_4);
		_calloutOverrideBtn.setText("=>"); //$NON-NLS-1$
		
		_callinBeforeBtn = new Button(_methMapBtnComp, SWT.TOGGLE);
		addButtonListener(_callinBeforeBtn);
		final FormData formData_5 = new FormData();
		formData_5.right = new FormAttachment(100, -5);
		formData_5.top = new FormAttachment(_calloutOverrideBtn, 5, SWT.BOTTOM);
		formData_5.left = new FormAttachment(0, 5);
		_callinBeforeBtn.setLayoutData(formData_5);
		_callinBeforeBtn.setText("<- before"); //$NON-NLS-1$
		
		_callinReplaceBtn = new Button(_methMapBtnComp, SWT.TOGGLE);
		addButtonListener(_callinReplaceBtn);
		final FormData formData_6 = new FormData();
		formData_6.right = new FormAttachment(100, -5);
		formData_6.top = new FormAttachment(_callinBeforeBtn, 5, SWT.BOTTOM);
		formData_6.left = new FormAttachment(0, 5);
		_callinReplaceBtn.setLayoutData(formData_6);
		_callinReplaceBtn.setText("<- replace"); //$NON-NLS-1$
		
		_callinAfterBtn = new Button(_methMapBtnComp, SWT.TOGGLE);
		addButtonListener(_callinAfterBtn);
		final FormData formData_7 = new FormData();
		formData_7.right = new FormAttachment(100, -5);
		formData_7.top = new FormAttachment(_callinReplaceBtn, 5, SWT.BOTTOM);
		formData_7.left = new FormAttachment(0, 5);
		_callinAfterBtn.setLayoutData(formData_7);
		_callinAfterBtn.setText("<- after"); //$NON-NLS-1$
		
		final Label roleMethLabel = new Label(this, SWT.NONE);
		final FormData formData = new FormData();
		formData.right = new FormAttachment(0, 210);
		formData.top = new FormAttachment(0, 5);
		formData.left = new FormAttachment(0, 5);
		roleMethLabel.setLayoutData(formData);
		roleMethLabel.setText(Messages.BindingConfiguration_role_methods_label);
		
		_roleMethListViewer = new TableViewer(this, SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
		_roleMethListViewer.setContentProvider(new RoleMethodContentProvider());
		_roleMethListViewer.setLabelProvider(labelProvider);
		final Table roleMethList = _roleMethListViewer.getTable();
		final FormData formData_8 = new FormData();
		formData_8.bottom = new FormAttachment(composite, 5, SWT.TOP);
		formData_8.right = new FormAttachment(40, 0);
		formData_8.top = new FormAttachment(roleMethLabel, 5, SWT.BOTTOM);
		formData_8.left = new FormAttachment(0, 5);
		roleMethList.setLayoutData(formData_8);
		_roleMethListViewer.setSorter(new ViewerSorter());
		_roleMethListViewer.setInput(new Object());   
		
		addRoleListSelectionChangedListener(_roleMethListViewer);
		
		final Label baseMethLabel = new Label(this, SWT.NONE);
		final FormData formData_2 = new FormData();
		formData_2.right = new FormAttachment(100, -5);
		formData_2.top = new FormAttachment(0, 5);
		formData_2.left = new FormAttachment(_methMapBtnComp, 5, SWT.RIGHT);
		baseMethLabel.setLayoutData(formData_2);
		baseMethLabel.setText(Messages.BindingConfiguration_base_methods_label);
		
		_baseMethListViewer = new TableViewer(this, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
		_baseMethListViewer.setContentProvider(new BaseMethodContentProvider());
		_baseMethListViewer.setLabelProvider(labelProvider);
		final Table baseMethList = _baseMethListViewer.getTable();
		final FormData formData_12 = new FormData();
		formData_12.bottom = new FormAttachment(composite, 5, SWT.TOP);
		formData_12.right = new FormAttachment(100, -5);
		formData_12.top = new FormAttachment(baseMethLabel, 5, SWT.BOTTOM);
		formData_12.left = new FormAttachment(60, 0);
		baseMethList.setLayoutData(formData_12);
		_baseMethListViewer.setSorter(new ViewerSorter());
		_baseMethListViewer.setInput(new Object());
		
		addBaseListSelectionChangedListener(_baseMethListViewer);
	}
	
	private void addRoleListSelectionChangedListener(final TableViewer tableViewer)
	{
		ISelectionChangedListener listener = new ISelectionChangedListener() 
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				if (validateSelections())
				{
					_methMapBtnComp.enableAll();
				}
				
				toggleApplyButton();
			}
		};
		
		tableViewer.addSelectionChangedListener(listener);
	}
	
	private void addBaseListSelectionChangedListener(final TableViewer tableViewer)
	{
		ISelectionChangedListener listener = new ISelectionChangedListener() 
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				if (!validateSelections())
				{
					return;
				}
				
				//no abstract methods selected
				_methMapBtnComp.enableAll();
				if (!tableViewer.getSelection().isEmpty()
						&& tableViewer.getTable().getSelection().length > 1)
				{
					toggleModifierButtons(OT_CALLOUT|OT_CALLOUT_OVERRIDE, DESELECT_DISABLE_BUTTONS);
					_methMapBtnComp.removeSelectionButton(_calloutBtn);
					_methMapBtnComp.removeSelectionButton(_calloutOverrideBtn);
				}
				toggleApplyButton();
			}
		};
		
		tableViewer.addSelectionChangedListener(listener);
	}
	
	private boolean validateSelections()
	{
		StructuredSelection selectedBaseIMembers = (StructuredSelection)_baseMethListViewer.getSelection();
		for (Iterator iter = selectedBaseIMembers.iterator(); iter.hasNext();)
		{
			IMember baseIMember = (IMember) iter.next();
			try
			{
				if (Flags.isAbstract(baseIMember.getFlags()))
				{
					_methMapBtnComp.deselectAll();
					_methMapBtnComp.disableAll();
					toggleApplyButton();
					return false;
				}
			}
			catch (JavaModelException ex)
			{
				return false;
			}
		}
		
		StructuredSelection selectedRoleIMethod = (StructuredSelection)_roleMethListViewer.getSelection();
		
		if (selectedRoleIMethod.isEmpty())
		{
			return true;
		}
		
		IMethod roleMethod = (IMethod)selectedRoleIMethod.getFirstElement();
		try 
		{
			if (roleMethod.getElementName().equals(FAKED_METHOD_NAME))
			{
				toggleModifierButtons(OT_CALLOUT_OVERRIDE|
						Modifier.OT_AFTER_CALLIN|
						Modifier.OT_BEFORE_CALLIN|
						Modifier.OT_REPLACE_CALLIN,
						DESELECT_DISABLE_BUTTONS);
				
				toggleApplyButton();
				return false;
			}
			
			if (!roleMethod.getElementName().equals(FAKED_METHOD_NAME)
					&& !Flags.isCallin(roleMethod.getFlags())
					&& !Flags.isAbstract(roleMethod.getFlags()))
			{
				toggleModifierButtons(Modifier.OT_REPLACE_CALLIN|OT_CALLOUT, DESELECT_DISABLE_BUTTONS);
				toggleApplyButton();
				return false;
			}
			
			if (!roleMethod.getElementName().equals(FAKED_METHOD_NAME)
					&& Flags.isCallin(roleMethod.getFlags())
					&& !Flags.isAbstract(roleMethod.getFlags()))
			{
				toggleModifierButtons(OT_CALLOUT_OVERRIDE|
						OT_CALLOUT|
						Modifier.OT_AFTER_CALLIN|
						Modifier.OT_BEFORE_CALLIN,
						DESELECT_DISABLE_BUTTONS);
				toggleApplyButton();
				return false;
			}
		} 
		catch (JavaModelException ex) 
		{
			return false;
		}
		
		return true;
	}
	
	protected void checkSubclass() {}
	
	public IMember[] getBaseMethods(IType baseClass) throws JavaModelException
	{        
		List<IMember> result = new ArrayList<IMember>();
		//FIXME: cleaner but way(!) too slow:
		//TypeHelper.getInheritedMethods(baseClass, true, false, false, new NullProgressMonitor());//
		
		// ---------- stolen (and adapted) from CallinMapping.findBaseMethods():
		IType[]   typeParents = TypeHelper.getSuperTypes(baseClass);
		if (OTModelManager.hasOTElementFor(baseClass)) {
			IOTType otType = OTModelManager.getOTElement(baseClass);
			if (otType.isRole()) {
				IType[] implicitSupers = TypeHelper.getImplicitSuperTypes((IRoleType)otType);
				int len1 = implicitSupers.length;
				int len2 = typeParents.length;
				// shift to back:
				System.arraycopy(
						typeParents, 0, 
						typeParents = new IType[len1+len2-1], 
							len1-1, // let tsupers overwrite first element which repeats the original baseClass
						len2); 
				// insert at front (implicit has higher priority)
				System.arraycopy(
						implicitSupers, 0, 
						typeParents, 0,
						len1);
			}
		}
		HashSet<String> signatures = new HashSet<String>(); // for filtering of duplicates.
		for (int i = 0; i < typeParents.length; i++) {
			if (typeParents[i].getFullyQualifiedName().equals(TypeHelper.JAVA_LANG_OBJECT)) 
				continue;
			// TODO(SH): don't include private fields from super classes
			IField[] fields = typeParents[i].getFields();
			for (int fieldIdx = 0; fieldIdx < fields.length; fieldIdx++)
			{
				IField field = fields[fieldIdx];
				String signature = field.getElementName()+field.getTypeSignature();
				if (!signatures.contains(signature))
				{
					result.add(field);
					signatures.add(signature);
				}
			}	
			
			IMethod[] methods = typeParents[i].getMethods();
			for (int methIdx = 0; methIdx < methods.length; methIdx++)
			{
				IMethod method = methods[methIdx];
				String signature = method.getElementName()+method.getSignature();
				if (   !method.isConstructor()
					&& !signatures.contains(signature))
				{
					result.add(method);
					signatures.add(signature);
				}
			}	
		}
		// -----------
		
		return result.toArray(new IMember[result.size()]);
	}
	
	public void setFocusRole(RoleTypeDeclaration focusRole, IType teamType, boolean initial)
	{
		if (initial)
		{
			_selectedCallinDecl = null;
			_selectedCalloutDecl = null;
			_parameterMappings = null;
		}
		
		_selectedRole = focusRole;
		
		IJavaProject project = teamType.getCompilationUnit().getJavaProject();
		IType teamClass = null;
		// implicit inherited role class methods    
		try 
		{
			teamClass = project.findType(teamType.getFullyQualifiedName('.'));
			_roleMethods = TypeHelper.getAllRoleMethods(
					teamClass,
					focusRole.getName().toString());
			
		} 
		catch (JavaModelException ex) 
		{
			openErrorDialog(Messages.BindingConfiguration_error_retrieving_role_methods);
		}
		
		Type baseType = focusRole.getBaseClassType();
							
		// base class methods
		if (baseType != null)
		{			
			try 
			{
				String qualifiedBaseName = null;
				if (baseType instanceof SimpleType) {
					// try for cached names (those roles created within the binding editor):
					Name baseName = ((SimpleType)baseType).getName();
					qualifiedBaseName = (baseName instanceof QualifiedName) ?
						baseName.getFullyQualifiedName() :
						this._bindingEditor._baseClassLookup.get(((SimpleName)baseName).getIdentifier());
				} 
				if (qualifiedBaseName == null) // other roles should be resolvable:
					qualifiedBaseName = focusRole.resolveBinding().getBaseClass().getQualifiedName(); 
				
				if (qualifiedBaseName != null)
					_baseMethods = getBaseMethods(project.findType(qualifiedBaseName));
				else
					OTDTUIPlugin.getExceptionHandler().logCoreException("Failed to resolve focus role - no methods available.", null); //$NON-NLS-1$
			}
			catch (JavaModelException ex)
			{
				OTDTUIPlugin.getExceptionHandler().logCoreException(ex.getMessage(), ex);
			}
		}
		
		// clear selection 
		_roleMethListViewer.setSelection(new StructuredSelection(EMPTY_LIST));
		_baseMethListViewer.setSelection(new StructuredSelection(EMPTY_LIST));
		_roleMethListViewer.refresh();
		_baseMethListViewer.refresh();
		_methMapBtnComp.enableAll();
		_methMapBtnComp.deselectAll();
		toggleApplyButton();
	}
	
	IType findRoleType (IType teamClass, String roleName)
		throws JavaModelException
	{
		IType[] roles = teamClass.getTypes();
		for (IType roleType : roles) 
			if (roleType.getElementName().equals(roleName))
				return roleType;
		
		for (IType roleType : roles) 
		{		
			IType result = findRoleType(roleType, roleName);
			if (result != null)
				return result;
		}		
		return null;
	}
	
	private void toggleApplyButton()
	{
		if (_baseMethListViewer.getSelection().isEmpty() 
				|| _roleMethListViewer.getSelection().isEmpty() 
				|| (_methMapBtnComp.getSelectedButton() == null))
		{
			_applyBtn.setEnabled(false);
		}
		else
		{
			_applyBtn.setEnabled(true);
		}
	}
	
	public void setCalloutMapping(ASTNode selectedNode, IType teamType)
	{
		if ( !(selectedNode instanceof CalloutMappingDeclaration) )
		{
			return;
		}
		
		_selectedCallinDecl = null;
		_selectedCalloutDecl = (CalloutMappingDeclaration)selectedNode;
		RoleTypeDeclaration roleTypeDecl = (RoleTypeDeclaration)_selectedCalloutDecl.getParent();
		setFocusRole(roleTypeDecl, teamType, false);
		
		MethodSpec roleMethSpec = (MethodSpec)_selectedCalloutDecl.getRoleMappingElement();        
		IMember roleMeth = findCorrespondingIMember(roleMethSpec, _roleMethods, null);
		if (roleMeth != null)
		{
			_roleMethListViewer.setSelection( new StructuredSelection(roleMeth) );
		}
		else
		{
			_roleMethListViewer.setSelection( new StructuredSelection(_fakedMeth) );            
		}
		
		MethodMappingElement baseSpec = (MethodMappingElement)_selectedCalloutDecl.getBaseMappingElement();
		List<MethodMappingElement> baseSpecs = new ArrayList<MethodMappingElement>();
		baseSpecs.add(baseSpec);
		List<IMember> curBaseMeths = findCorrespondingIMembers(baseSpecs, _baseMethods);
		if (curBaseMeths != null)
		{
			_baseMethListViewer.setSelection( new StructuredSelection(curBaseMeths) );
		}
		else
		{
			_baseMethListViewer.setSelection( new StructuredSelection(EMPTY_LIST) );            
		}
		
		_parameterMappings = _selectedCalloutDecl.getParameterMappings();
		
		
		if( _selectedCalloutDecl.isCalloutOverride())
		{
			_methMapBtnComp.setSelectionButton(_calloutOverrideBtn);
		}
		else
		{
			_methMapBtnComp.setSelectionButton(_calloutBtn);
		}
		
		toggleApplyButton();
	}
	
	public void setCallinMapping(ASTNode selectedNode, IType teamType)
	{
		if ( !(selectedNode instanceof CallinMappingDeclaration))
		{
			return;
		}
		
		_selectedCalloutDecl = null;
		_selectedCallinDecl = (CallinMappingDeclaration)selectedNode;
		
		RoleTypeDeclaration roleTypeDecl = (RoleTypeDeclaration)_selectedCallinDecl.getParent();
		setFocusRole(roleTypeDecl, teamType, false);
		
		MethodSpec roleMethSpec = (MethodSpec)_selectedCallinDecl.getRoleMappingElement();
		IMember roleMeth = findCorrespondingIMember(roleMethSpec, _roleMethods, null);
		if (roleMeth != null)
		{
			_roleMethListViewer.setSelection( new StructuredSelection(roleMeth) );                
		}
		else
		{
			_roleMethListViewer.setSelection( new StructuredSelection(EMPTY_LIST) );            
		}
		
		java.util.List<MethodMappingElement> baseMapElems = _selectedCallinDecl.getBaseMappingElements();
		if (baseMapElems.size() == 0)
		{
			return;
		}
		
		java.util.List<IMember> baseMembs = findCorrespondingIMembers(baseMapElems, _baseMethods);
		if (baseMembs != null)
		{
			_baseMethListViewer.setSelection( new StructuredSelection(baseMembs) );
		}
		else
		{
			_baseMethListViewer.setSelection( new StructuredSelection(EMPTY_LIST) );            
		}
		
		_parameterMappings = _selectedCallinDecl.getParameterMappings();
		
		toggleModifierButtons(_selectedCallinDecl.getCallinModifier(), SELECT_BUTTONS);
		toggleApplyButton();
	}
	
	private List<IMember> findCorrespondingIMembers(List<MethodMappingElement> methodSpecs, IMember[] members)
	{
		Hashtable<String, Integer> methodAppearances = getMethodAppearances(members);
		
		List<IMember> baseMembers = new ArrayList<IMember>();
		for (Iterator<MethodMappingElement> iter = methodSpecs.iterator(); iter.hasNext();)
		{
			MethodMappingElement methodSpec = iter.next();
			
			IMember foundMethod = findCorrespondingIMember(methodSpec, members, methodAppearances);
			baseMembers.add(foundMethod);
		}
		return baseMembers;
	}
	
	private IMember findCorrespondingIMember(MethodMappingElement methodSpec, IMember[] methods, Hashtable<String, Integer> methodAppearances)
	{
		if (methodAppearances == null)
			methodAppearances = getMethodAppearances(methods);
		
		String methodName = methodSpec.getName().toString();
		for (int idx = 0; idx < methods.length; idx++)
		{
			if (methodName.equals(methods[idx].getElementName()))
			{
				Integer value = methodAppearances.get(methodName);
				if (value.intValue() > 1)
				{
					// FIXME(SH): handle field cases:
					if (   methodSpec instanceof MethodSpec
						&& methods[idx] instanceof IMethod) 
					{
						List parameters = ((MethodSpec)methodSpec).parameters();
						String []parameterTypes = ((IMethod)methods[idx]).getParameterTypes();
						
						if (parameters.size() != parameterTypes.length)
						{
							continue;
						}
						
						if (validateParameterMatching(parameters, parameterTypes))
						{
							return methods[idx];
						}
					}
				}
				else
				{
					return methods[idx];
				}
			}   
		}		
		return null;
	}
	
	private boolean validateParameterMatching(List parameters, String[] parameterTypes)
	{
		boolean totallyMatch = true;
		int counter = 0;
		for (Iterator iter = parameters.iterator(); iter.hasNext();)
		{
			SingleVariableDeclaration variable = (SingleVariableDeclaration) iter .next();
			String paramTypeNameFromMethodSpec = null;
			
			if (variable.getType().isPrimitiveType())
			{
				paramTypeNameFromMethodSpec = ((PrimitiveType)variable.getType()).getPrimitiveTypeCode().toString();
			}
			
			if (variable.getType().isSimpleType())
			{
				ITypeBinding typeBinding =((SimpleType)variable.getType()).resolveBinding();
				if (typeBinding == null)
				{
					paramTypeNameFromMethodSpec = ((SimpleType)variable.getType()).getName().getFullyQualifiedName();
				}
				else
				{
					paramTypeNameFromMethodSpec = typeBinding.getQualifiedName();
				}
			}
			
			if (variable.getType().isArrayType())
			{
				Type arrayType = ((ArrayType)variable.getType()).getComponentType();
				if (arrayType.isPrimitiveType())
				{
					paramTypeNameFromMethodSpec = ((PrimitiveType)arrayType).getPrimitiveTypeCode().toString();
				}
				
				if (arrayType.isSimpleType())
				{
					ITypeBinding typeBinding =((SimpleType)arrayType).resolveBinding();
					if (typeBinding == null)
					{
						paramTypeNameFromMethodSpec = ((SimpleType)arrayType).getName().getFullyQualifiedName();
					}
					else
					{
						paramTypeNameFromMethodSpec = typeBinding.getQualifiedName();
					}
				}
				paramTypeNameFromMethodSpec += "[]"; //$NON-NLS-1$
			}
			
			//TODO(ike): Find a way to resolved parameters of methodSpecs to get fully qualified names
			String paramTypeNameFromIMethod = Signature.toString(parameterTypes[counter]);
			String simpleTypeName = getSimpleParameterName(paramTypeNameFromIMethod);
			if (!simpleTypeName.equals(paramTypeNameFromMethodSpec))
			{
				totallyMatch = false;
			}
			
			counter++;
		}
		return totallyMatch;
	}
	
	
	private String getSimpleParameterName(String parameterName)
	{
		String [] simpleNameArray = parameterName.split("\\."); //$NON-NLS-1$
		if (simpleNameArray.length == 0)
			return parameterName;
		else
			return simpleNameArray[simpleNameArray.length - 1];
	}
	
	private Hashtable<String, Integer> getMethodAppearances(IMember[] methods)
	{
		Hashtable<String, Integer> appearances = new Hashtable<String, Integer>(); 
		for (int idx = 0; idx < methods.length; idx++)
		{
			String methodName = methods[idx].getElementName();
			if (appearances.containsKey(methodName))
			{
				Integer value = appearances.get(methodName);
				int app = value.intValue();
				app++;
				appearances.put(methodName, new Integer(app));
			}
			else
			{
				appearances.put(methodName, new Integer(1));
			}
		}
		return appearances;
	}
	
	private void toggleModifierButtons(int modifiers, int selectionLevel)
	{
		List<Button> buttonList = getButtonsForModifiers(modifiers);
		
		switch (selectionLevel) 
		{
		case SELECT_BUTTONS:
			for (Iterator<Button> iter = buttonList.iterator(); iter.hasNext();)
			{
				Button button = iter.next();
				_methMapBtnComp.setSelectionButton(button);
			}
			break;
			
		case DESELECT_DISABLE_BUTTONS:
			if (Modifier.isReplace(modifiers))
			{
				_callinReplaceBtn.setEnabled(false);
				_methMapBtnComp.removeSelectionButton(_callinReplaceBtn);
			}
			else
			{
				_callinReplaceBtn.setEnabled(true);
			}
			
			if (Modifier.isBefore(modifiers))
			{
				_callinBeforeBtn.setEnabled(false);
				_methMapBtnComp.removeSelectionButton(_callinBeforeBtn);
			}
			else
			{
				_callinBeforeBtn.setEnabled(true);
			}
			
			if (Modifier.isAfter(modifiers))
			{
				_callinAfterBtn.setEnabled(false);
				_methMapBtnComp.removeSelectionButton(_callinAfterBtn);
			}
			else
			{
				_callinAfterBtn.setEnabled(true);	        	
			}
			
			if (isCallout(modifiers))
			{
				_calloutBtn.setEnabled(false);
				_methMapBtnComp.removeSelectionButton(_calloutBtn);
			}
			else
			{
				_calloutBtn.setEnabled(true);
			}
			
			if (isCalloutOverride(modifiers))
			{
				_calloutOverrideBtn.setEnabled(false);
				_methMapBtnComp.removeSelectionButton(_calloutOverrideBtn);
			}
			else
			{
				_calloutOverrideBtn.setEnabled(true);
			}
			break;
			
		default:
			break;
		}
	}
	
	private List<Button> getButtonsForModifiers(int modifiers)
	{
		List<Button> buttonList = new ArrayList<Button>();
		
		if (Modifier.isReplace(modifiers))
			buttonList.add(_callinReplaceBtn);
		
		if (Modifier.isBefore(modifiers))
			buttonList.add(_callinBeforeBtn);
		
		if (Modifier.isAfter(modifiers))
			buttonList.add(_callinAfterBtn);
		
		if (isCallout(modifiers))
			buttonList.add(_calloutBtn);
		
		if (isCalloutOverride(modifiers))
			buttonList.add(_calloutBtn);
		
		return buttonList;
	}
	
	private boolean isCalloutOverride(int modifiers)
	{
		return (modifiers & OT_CALLOUT_OVERRIDE) != 0;
	}
	
	private boolean isCallout(int modifiers)
	{
		return (modifiers & OT_CALLOUT) != 0;
	}
	
	private void addButtonListener(Button button)
	{
		SelectionListener buttonListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent evt)
			{
				Button selectedButton = (Button)evt.getSource();
				_methMapBtnComp.doRadioButtonBehavior(selectedButton);
				toggleApplyButton();
			}
			
			public void widgetDefaultSelected(SelectionEvent evt) {}
		};            
		button.addSelectionListener(buttonListener);
	}
	
	private void addApplyButtonListener(Button applyButton)
	{
		SelectionListener applyButtonListener = new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent evt)
			{
				applyPressed();
			}
		};  
		applyButton.addSelectionListener(applyButtonListener);
	}
	
	private boolean createMethodMapping()
	{
		int methMapModifier = 0;
		boolean calloutOverride = false;
		boolean signatureFlag = true;
		_newCallout = true;
		Button selectedButton = _methMapBtnComp.getSelectedButton();
		
		if (_calloutBtn.equals(selectedButton))
		{
			methMapModifier = 0;
			_newCallout = true;
		}
		if (_calloutOverrideBtn.equals(selectedButton))
		{
			calloutOverride = true;
			methMapModifier = 0;
			_newCallout = true;
		}
		if (_callinReplaceBtn.equals(selectedButton))
		{
			methMapModifier = Modifier.OT_REPLACE_CALLIN;
			_newCallout = false;
		}
		if (_callinBeforeBtn.equals(selectedButton))
		{
			methMapModifier = Modifier.OT_BEFORE_CALLIN;
			_newCallout = false;
		}
		if (_callinAfterBtn.equals(selectedButton))
		{
			methMapModifier = Modifier.OT_AFTER_CALLIN;
			_newCallout = false;
		}
		
		StructuredSelection selectedRoleMethod = (StructuredSelection)_roleMethListViewer.getSelection();
		if (selectedRoleMethod.isEmpty())
		{
			return false;
		}
		
		StructuredSelection selectedBaseMethods = (StructuredSelection)_baseMethListViewer.getSelection();
		if (selectedBaseMethods.isEmpty())
		{
			return false;
		}
		
		IMethod roleMethod = (IMethod) selectedRoleMethod.getFirstElement();
		IMember[] baseMethods = new IMember[selectedBaseMethods.size()];
		int baseMethodsCount = 0;
		for (Iterator iter = selectedBaseMethods.iterator(); iter.hasNext();)
		{
			IMember baseMethod = (IMember) iter.next();
			baseMethods[baseMethodsCount++] = baseMethod;
		}
		
		AST ast = _selectedRole.getAST();
		
		if (_newCallout)
		{
			return createCalloutMapping(ast, roleMethod, baseMethods[0], methMapModifier, calloutOverride, signatureFlag);
		}
		else
		{
			return createCallinMapping(ast, roleMethod, baseMethods, methMapModifier, signatureFlag);
		}
	}
	
	private boolean createCallinMapping(AST ast, IMethod roleIMethod, IMember[] baseMethods, int modifier, boolean signatureFlag)
	{
		// can only bind methods in callin:
		for (IMember member : baseMethods) 
			if (!(member instanceof IMethod))
				return false;
		
		MethodSpec givenRoleMSpec = null;
		List<MethodSpec> givenBaseMSpecs = null;
		if (_selectedCallinDecl !=  null)
		{
			givenRoleMSpec  = (MethodSpec)_selectedCallinDecl.getRoleMappingElement();
			givenBaseMSpecs = _selectedCallinDecl.getBaseMappingElements();
		}
		
		IMethod templateForRoleMethodSpec = roleIMethod;
		if (templateForRoleMethodSpec.getElementName().startsWith(FAKED_METHOD_NAME))
			templateForRoleMethodSpec = (IMethod)baseMethods[0]; // use the first base method as template for the role method spec
		// FIXME: support automatic creation of a new role method
		
		MethodSpec roleMethodSpec = createMethodSpec(ast, templateForRoleMethodSpec, givenRoleMSpec, signatureFlag);
		if (roleMethodSpec == null)
			return false;
		
		List<MethodMappingElement> baseMethodSpecs = new ArrayList<MethodMappingElement>();
		for (int idx = 0; idx < baseMethods.length; idx++)
		{
			IMethod baseIMethod = (IMethod)baseMethods[idx];
			MethodSpec baseMSpec = null;
			
			if ((givenBaseMSpecs != null) && baseMethods.length == 1)
			{
				baseMSpec = givenBaseMSpecs.get(idx);
			}
			
			MethodMappingElement baseMethodSpec = createMethodSpec(ast, baseIMethod, baseMSpec, signatureFlag);
			if (baseMethodSpec!= null)
			{
				baseMethodSpecs.add(baseMethodSpec);
			}
		}
		if (_selectedCallinDecl == null)
		{
			_parameterMappings = null;
		}
		
		_callinMapping =  ASTNodeCreator.createCallinMappingDeclaration(
				ast, 
				null, 
				modifier, 
				roleMethodSpec, 
				baseMethodSpecs, 
				_parameterMappings);
         
		return true;
	}
	
	private boolean createCalloutMapping(
			AST ast, 
			IMethod roleMethod, 
			IMember baseMethod, 
			int bindingModifier, 
			boolean calloutOverride, 
			boolean signatureFlag)
	{
		MethodSpec selRolMethSpec = null;
		MethodMappingElement selBasMethSpec = null;
		
		if (_selectedCalloutDecl !=  null)
		{
			selRolMethSpec = (MethodSpec)_selectedCalloutDecl.getRoleMappingElement();
			selBasMethSpec = _selectedCalloutDecl.getBaseMappingElement();
			if (baseMethod instanceof IMethod && !(selBasMethSpec instanceof MethodSpec))
				return false; // cannot change from method to field.
		}
		
		MethodMappingElement baseMethodSpec =  null;
		if (baseMethod instanceof IMethod)
			baseMethodSpec = createMethodSpec(
				ast, 
				(IMethod)baseMethod, 
				(MethodSpec)selBasMethSpec, 
				signatureFlag);
		/* FIXME: implement all other combinations
		else
			baseMethodSpec = createFieldAccessSpec(ast, (IField)baseIMember, baseMSpec, signatureFlag);
		*/
		if (baseMethodSpec == null)
		{
			return false;
		}    
		
		MethodMappingElement roleMethodSpec = null;
		if (roleMethod.getElementName().startsWith(FAKED_METHOD_NAME))
		{
			if (baseMethod instanceof IMethod)
				roleMethodSpec = createMethodSpec(
						ast, 
						(IMethod)baseMethod, 
						(MethodSpec)selBasMethSpec, 
						signatureFlag);
			/* FIXME: implement: 
			else
				roleMethodSpec = createFieldAccess((IField)baseMethod...);
			 */  
		}
		else
		{
			roleMethodSpec = createMethodSpec(
					ast, 
					roleMethod, 
					selRolMethSpec, 
					signatureFlag);
		}
		if (roleMethodSpec == null)
		{
			return false;
		}
		
		if (_selectedCalloutDecl == null)
		{
			_parameterMappings = null;
		}
		
		_calloutMapping =  ASTNodeCreator.createCalloutMappingDeclaration(
				ast, 
				null, 
				0, // modifiers 
				roleMethodSpec, 
				baseMethodSpec, 
				bindingModifier, // FIXME(SH): not yet provided from caller
				_parameterMappings, 
				calloutOverride, 
				signatureFlag);

		return true;
	}

	private MethodSpec createMethodSpec(AST ast, IMethod iMethod, MethodSpec givenMethodSpec, boolean signatureFlag)
	{
		String returnTypeString;
		try
		{
			returnTypeString = Signature.toString(iMethod.getReturnType());
		}
		catch (JavaModelException e)
		{
			return null;    
		}
		
		Type returnType = ASTNodeCreator.createType(ast, returnTypeString);
		
		String [] parameterTypes = iMethod.getParameterTypes();
		String [] parameterNames = null;
		
		try
		{
			parameterNames = iMethod.getParameterNames();            
		}
		catch (JavaModelException e1)
		{
			return null;
		}
		
		List mSpecParameters = null;
		if (givenMethodSpec != null)
		{
			mSpecParameters = givenMethodSpec.parameters();
		}
		
		java.util.List<SingleVariableDeclaration> methodParameters = new ArrayList<SingleVariableDeclaration>();
		for (int idx = 0; idx < parameterTypes.length; idx++)
		{
			Type parameterType = ASTNodeCreator.createType(
					ast, 
					Signature.getSimpleName(Signature.toString(parameterTypes[idx])));
			
			SingleVariableDeclaration roleParameter;
			if (mSpecParameters != null && mSpecParameters.size()==parameterNames.length)
			{
				SingleVariableDeclaration param = (SingleVariableDeclaration)mSpecParameters.get(idx);
				roleParameter = 
					ASTNodeCreator.createArgument(ast, 0, parameterType, param.getName().toString(), 0, null);
			}
			else
			{
				roleParameter = 
					ASTNodeCreator.createArgument(ast, 0, parameterType, parameterNames[idx], 0, null);
			}

			methodParameters.add(roleParameter);

		}
		
		MethodSpec methodSpec = ASTNodeCreator.createMethodSpec(
				ast,
				iMethod.getElementName(),
				returnType,
				methodParameters,
				signatureFlag
		);

		
		return methodSpec;
	}
	
	//copied from org.soothsayer.util.ASTNodeHelper;
	public static String getMethodSignature(IMethod meth)
	{
		StringBuffer result = new StringBuffer();
		
		if (meth != null)
		{
			result.append(meth.getElementName());
			result.append('(');
			
			String[] parameterTypes = meth.getParameterTypes();
			for (int idx = 0; idx < parameterTypes.length; idx++)
			{
				String curType = parameterTypes[idx];
				result.append(curType);
				if (idx < parameterTypes.length)
				{
					result.append(", "); //$NON-NLS-1$
				}
			}
			result.append(')');
		}
		
		return result.toString();
	}
	
	public void resetLists()
	{
		_baseMethListViewer.getTable().removeAll();
		_roleMethListViewer.getTable().removeAll();
		_methMapBtnComp.enableAll();
		_methMapBtnComp.deselectAll();
	}
	
	public void setCurrentTeamForMethodFake(IType teamType)
	{
		_curTeam = teamType;
	}
	
	private void applyPressed() 
	{
		if (_selectedRole.isRoleFile()) {
			openErrorDialog(MessageFormat.format(
									Messages.BindingConfiguration_error_cant_edit_rolefile,
									_selectedRole.getName().getIdentifier(),
									_selectedRole.getName().getIdentifier()));
			return;
		}
		RoleTypeDeclaration currentRole= _selectedRole;
		while (true) {
			currentRole= (RoleTypeDeclaration)ASTNodes.getParent(currentRole, ASTNode.ROLE_TYPE_DECLARATION);
			if (currentRole == null)
				break;
			if (currentRole.isRoleFile()) {
				openErrorDialog(MessageFormat.format(
						Messages.BindingConfiguration_error_cant_edit_rolefile_nested,
						new Object[]{_selectedRole.getName().getIdentifier(),
						currentRole.getName().getIdentifier(),
						currentRole.getName().getIdentifier()}));
				return;
			}
			currentRole= (RoleTypeDeclaration)ASTNodes.getParent(_selectedRole, ASTNode.ROLE_TYPE_DECLARATION);
		}
		
		_calloutMapping = null;
		_callinMapping = null;
		int selectedIndex = -1;
		
		if (createMethodMapping())
		{
			if (_calloutMapping != null)
			{
				if (_selectedCalloutDecl != null)
				{
					selectedIndex = _selectedRole.bodyDeclarations().indexOf(_selectedCalloutDecl);
					_selectedRole.bodyDeclarations().remove(_selectedCalloutDecl);
				}
				if (_selectedCallinDecl != null)
				{
					selectedIndex = _selectedRole.bodyDeclarations().indexOf(_selectedCallinDecl);
					_selectedRole.bodyDeclarations().remove(_selectedCallinDecl);
				}
				
				if (selectedIndex == -1){
					_selectedRole.bodyDeclarations().add(_calloutMapping);
				}
				else
				{
					_selectedRole.bodyDeclarations().add(selectedIndex, _calloutMapping);
				}
			}
			
			if (_callinMapping != null)
			{
				if (_selectedCallinDecl != null)
				{
					selectedIndex = _selectedRole.bodyDeclarations().indexOf(_selectedCallinDecl);
					_selectedRole.bodyDeclarations().remove(_selectedCallinDecl);
					
				}
				if (_selectedCalloutDecl != null)
				{
					selectedIndex = _selectedRole.bodyDeclarations().indexOf(_selectedCalloutDecl);
					_selectedRole.bodyDeclarations().remove(_selectedCalloutDecl);
				}
				
				if (selectedIndex == -1)
				{
					_selectedRole.bodyDeclarations().add(_callinMapping);
				}
				else
				{
					_selectedRole.bodyDeclarations().add(selectedIndex, _callinMapping);
				}
			}
			
			// clear selection
			_roleMethListViewer.setSelection(new StructuredSelection(EMPTY_LIST));
			_baseMethListViewer.setSelection(new StructuredSelection(EMPTY_LIST));
			_roleMethListViewer.refresh();
			_baseMethListViewer.refresh();
			
			_bindingEditor.refresh();
			
			AbstractMethodMappingDeclaration mapping = _callinMapping;
			if (mapping == null)
				mapping = _calloutMapping;
			_bindingEditor.methodBindingAdded(mapping);
		}
		else
		{
			openErrorDialog(Messages.BindingConfiguration_error_binding_creation_failed);
			
			_roleMethListViewer.setSelection(new StructuredSelection(EMPTY_LIST));
			_baseMethListViewer.setSelection(new StructuredSelection(EMPTY_LIST));
			_roleMethListViewer.refresh();
			_baseMethListViewer.refresh();
			_methMapBtnComp.enableAll();
			_methMapBtnComp.deselectAll();
			
			_bindingEditor.refresh();
		}
	}
	
	private void openErrorDialog(String message)
	{
		MessageDialog.openError(getShell(), Messages.BindingConfiguration_error_unspecific, message);
	}
	
}
