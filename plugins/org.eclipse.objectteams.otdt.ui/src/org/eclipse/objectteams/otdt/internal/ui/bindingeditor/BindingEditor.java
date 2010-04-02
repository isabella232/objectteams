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
 * $Id: BindingEditor.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.bindingeditor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractMethodMappingDeclaration;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccessSpec;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodMappingElement;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterMapping;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.ui.ImageManager;

/**
 * The main UI class of the binding editor.
 * 
 * @author jwloka
 */
public class BindingEditor extends Composite
{
    private IType _team; 
    final BindingConfiguration bindConfig;
    private Tree _connTableTree;
    private TabFolder _tabFolder;
    private CallinMappingConfiguration _callinMapConfig;
    private CalloutMappingConfiguration _calloutMapConfig;
    private TabItem _paraMapItem;
    private TabItem _methMapItem;
    // for tab folder switching
    private final TabItem[] _methMapItems;
    private final TabItem[] _paraMapItems;
    final TreeViewer _connTableViewer;
	private TypeDeclaration _rootTeam;
    
	/** Map single names to qualified names (only for bases of newly created roles. */
    HashMap<String,String> _baseClassLookup = new HashMap<String,String>();
	
    class TreeContentProvider implements IStructuredContentProvider, ITreeContentProvider 
    {
    	    	
    	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
    	public void dispose() {}
        
    	public Object[] getElements(Object inputElement) 
    	{
        	TypeDeclaration decl = (TypeDeclaration)inputElement;
        	return decl.getTypes();
    	}
        
    	public Object[] getChildren(Object parentElement) 
    	{
        	ASTNode node = (ASTNode)parentElement;
        	if (node.getNodeType() == ASTNode.ROLE_TYPE_DECLARATION) 
        	{
        		RoleTypeDeclaration roleNode = (RoleTypeDeclaration)node;
        		Object[] callouts = roleNode.getCallOuts();
        		Object[] callins = roleNode.getCallIns();
        		Object[] roles = roleNode.getRoles();
        		Object[] result = new Object[roles.length+callouts.length+callins.length];
        		System.arraycopy(roles, 0, result, 0, roles.length);
        		System.arraycopy(callouts, 0, result, roles.length, callouts.length);
        		System.arraycopy(callins, 0, result, roles.length+callouts.length, callins.length);
        		return result;
        	}
			else if (node.getNodeType() == ASTNode.CALLIN_MAPPING_DECLARATION 
			        	|| node.getNodeType() == ASTNode.CALLOUT_MAPPING_DECLARATION)
			{
				AbstractMethodMappingDeclaration mapping = 
					(AbstractMethodMappingDeclaration)node;
				List pMappings = mapping.getParameterMappings();
				return pMappings.toArray();
			}
			else
			{
				return new Object[]{};
			}
    	}
    	
        public Object getParent(Object element) 
        {
            return null;
        }
        
        public boolean hasChildren(Object element) 
        {
            return getChildren(element).length > 0;
        }
    }
    
    class LabelProvider extends WorkbenchLabelProvider implements ITableLabelProvider
	{
		private static final String INDENT = "    "; //$NON-NLS-1$
		private static final String PARAMETER_MAPPING_RIGHT = "->"; //$NON-NLS-1$
        private static final String EMPTY_TEXT = ""; //$NON-NLS-1$
        
        public void addListener(ILabelProviderListener listener) {}
		public void dispose() {}

		public boolean isLabelProperty(Object element, String property) 
		{
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {}

		public Image getColumnImage(Object element, int columnIndex) 
		{
			ASTNode node = (ASTNode)element;
			switch (columnIndex) {
			case 0: // role column
				if (node.getNodeType() == ASTNode.ROLE_TYPE_DECLARATION) {
					if (((RoleTypeDeclaration)node).isTeam())
						return ImageManager.getSharedInstance().get(ImageManager.TEAM_ROLE_IMG);
					else
						return ImageManager.getSharedInstance().get(ImageManager.ROLECLASS_IMG);
				} 
				break;
			case 2: // base column
				if (node.getNodeType() == ASTNode.ROLE_TYPE_DECLARATION) {
					ITypeBinding binding= ((TypeDeclaration)node).resolveBinding();
					if (binding != null) 
						binding= binding.getBaseClass();
					if (binding != null) {
						if (binding.isRole()) {
							if (binding.isTeam())
								return ImageManager.getSharedInstance().get(ImageManager.TEAM_ROLE_IMG);
							else
								return ImageManager.getSharedInstance().get(ImageManager.ROLECLASS_IMG);							
						} else if (binding.isTeam()) {
							return ImageManager.getSharedInstance().get(ImageManager.TEAM_IMG);							
						}
					}
					return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
				} else if (node.getNodeType() == ASTNode.CALLOUT_MAPPING_DECLARATION) {
					MethodMappingElement baseMappingElement = ((CalloutMappingDeclaration)node).getBaseMappingElement();
					if (baseMappingElement.getNodeType() == ASTNode.FIELD_ACCESS_SPEC)
					{
						IVariableBinding field= ((FieldAccessSpec)baseMappingElement).resolveBinding();
						if (field != null) {
							if (Modifier.isPublic(field.getModifiers()))
								return JavaUI.getSharedImages().getImage(ISharedImages.IMG_FIELD_PUBLIC);
							if (Modifier.isProtected(field.getModifiers()))
								return JavaUI.getSharedImages().getImage(ISharedImages.IMG_FIELD_PROTECTED);
							if (Modifier.isPrivate(field.getModifiers()))
								return JavaUI.getSharedImages().getImage(ISharedImages.IMG_FIELD_PRIVATE);
						}						
						return JavaUI.getSharedImages().getImage(ISharedImages.IMG_FIELD_DEFAULT);
					}
				}
				break;
			case 1: // mapping kind column
				switch (node.getNodeType()) {
				case ASTNode.CALLIN_MAPPING_DECLARATION:
					int callinModifier = ((CallinMappingDeclaration)element).getCallinModifier();
					if (Modifier.isAfter(callinModifier))
						return ImageManager.getSharedInstance().get(ImageManager.CALLINBINDING_AFTER_IMG);
					else if(Modifier.isBefore(callinModifier))
						return ImageManager.getSharedInstance().get(ImageManager.CALLINBINDING_BEFORE_IMG);
					else
						return ImageManager.getSharedInstance().get(ImageManager.CALLINBINDING_REPLACE_IMG);

				case ASTNode.CALLOUT_MAPPING_DECLARATION:
					return ImageManager.getSharedInstance().get(ImageManager.CALLOUTBINDING_IMG);
				
				case ASTNode.PARAMETER_MAPPING:
					ParameterMapping mapping = (ParameterMapping)element;
					if (mapping.getDirection().equals(PARAMETER_MAPPING_RIGHT))
						return ImageManager.getSharedInstance().get(ImageManager.CALLOUTBINDING_IMG);
					else
						return ImageManager.getSharedInstance().get(ImageManager.CALLINBINDING_REPLACE_IMG); // ups ;-)
				}
			}
			return null;
		}
		

		public String getColumnText(Object element, int columnIndex) 
		{
			ASTNode node = (ASTNode)element;
			switch (node.getNodeType()) 
			{
				case ASTNode.ROLE_TYPE_DECLARATION:
				{
					RoleTypeDeclaration role = (RoleTypeDeclaration)element;
					switch (columnIndex)
					{
						case 0:
							return role.getName().getIdentifier();
						case 1:
							return EMPTY_TEXT;
						case 2:
							Name baseClass = getRoleBaseClass(role);
							if (baseClass != null)
								return baseClass.getFullyQualifiedName();
							else 
								return EMPTY_TEXT;
						default:
							return EMPTY_TEXT;
					}
				}
				case ASTNode.CALLIN_MAPPING_DECLARATION:
				{
					CallinMappingDeclaration callinDecl = (CallinMappingDeclaration)element;
					switch (columnIndex)
					{
						case 0:
							return callinDecl.getRoleMappingElement().toString();
						case 1:
						    
						    if (Modifier.isAfter(callinDecl.getCallinModifier()))
							{
								return new String(IOTConstants.NAME_AFTER);
							}
							else if(Modifier.isBefore(callinDecl.getCallinModifier()))
							{
							    return new String(IOTConstants.NAME_BEFORE);
							}
							else if(Modifier.isReplace(callinDecl.getCallinModifier()))
							{
							    return new String(IOTConstants.NAME_REPLACE);
							}
							else
							{
							    return EMPTY_TEXT;
							}
						    
						    //orig:
							//return Modifier.ModifierKeyword.fromFlagValue(callinDecl.getModifiers()).toString();
						case 2:
							List baseMappingElements = callinDecl.getBaseMappingElements(); 
							String result = INDENT;
							for (int idx = 0; idx < baseMappingElements.size(); idx ++)
							{
								if (idx > 0)
								{
									result = result + ", "; //$NON-NLS-1$
								}
								result = result + baseMappingElements.get(idx).toString();
							}
							return result;
						default:
							return EMPTY_TEXT;
					}
				}
				case ASTNode.CALLOUT_MAPPING_DECLARATION:
				{
					CalloutMappingDeclaration callout = (CalloutMappingDeclaration)element;
					switch (columnIndex)
					{
						case 0:
							return callout.getRoleMappingElement().toString();
						case 1:
							return EMPTY_TEXT;
						case 2:
							return INDENT+callout.getBaseMappingElement().toString();
						default:
							return EMPTY_TEXT;
					}
				}	
				case ASTNode.PARAMETER_MAPPING:
				{
					ParameterMapping mapping = (ParameterMapping)element;
					switch (columnIndex)
					{
						case 0:
							if (mapping.getDirection().equals(PARAMETER_MAPPING_RIGHT))
								return mapping.getExpression().toString();
							else
				            	return mapping.getIdentifier().getIdentifier();
						case 1:
							return EMPTY_TEXT;
						case 2:
							if (mapping.getDirection().equals(PARAMETER_MAPPING_RIGHT))
								return INDENT+mapping.getIdentifier();
							else
								return INDENT+mapping.getExpression().toString();
						default:
							return EMPTY_TEXT;
					}
				}
				default:
				{
					return EMPTY_TEXT;
				}
			}
		}
	}
    
    
    public BindingEditor(final Composite parent, int style, final IType teamType, final CompilationUnit root)
    {
        super(parent, style);
        _team = teamType;
        calculateRootNode(root);
        
        setLayout(new FormLayout());

        final Group connDefGroup = new Group(this, SWT.NONE);
        connDefGroup.setText(Messages.BindingEditor_connector_title);
        final FormData formData = new FormData();
        formData.bottom = new FormAttachment(58, 0);
        formData.right = new FormAttachment(100, -5);
        formData.top = new FormAttachment(0, 5);
        formData.left = new FormAttachment(0, 5);
        connDefGroup.setLayoutData(formData);
        connDefGroup.setLayout(new FormLayout());

        _connTableViewer = new TreeViewer(connDefGroup, SWT.BORDER);
        _connTableViewer.setContentProvider(new TreeContentProvider());
        _connTableViewer.setLabelProvider(new LabelProvider());
        _connTableViewer.setAutoExpandLevel(2);
        
        _connTableTree = _connTableViewer.getTree();

        final FormData formData_2 = new FormData();
        formData_2.bottom = new FormAttachment(84, 0);
        formData_2.right = new FormAttachment(100, -5);
        formData_2.top = new FormAttachment(0, 5);
        formData_2.left = new FormAttachment(0, 5);
        _connTableTree.setLayoutData(formData_2);
        _connTableTree.addSelectionListener( new SelectionAdapter() 
        {
            public void widgetSelected(SelectionEvent evt) 
            {
                bindingTableSelectionChanged();
            }
         });
        
        _connTableTree.setLinesVisible(true);
        _connTableTree.setHeaderVisible(true);
        
        final TreeColumn rolesCol = new TreeColumn(_connTableTree, SWT.NONE);
        rolesCol.setWidth(300);
        rolesCol.setText(Messages.BindingEditor_role_types_title);

        final TreeColumn methMapColumn = new TreeColumn(_connTableTree, SWT.NONE);
        methMapColumn.setWidth(80);

        final TreeColumn baseCol = new TreeColumn(_connTableTree, SWT.NONE);
        baseCol.setWidth(300);
        baseCol.setText(Messages.BindingEditor_base_types_title);

		// Note(SH): need all columns set before retrieving contents by setInput()
        _connTableViewer.setInput(_rootTeam);
        
        final Composite buttonComp = new Composite(connDefGroup, SWT.NONE);
        final FormData formData_3 = new FormData();
        formData_3.bottom = new FormAttachment(100, -5);
        formData_3.right = new FormAttachment(100, -5);
        formData_3.top = new FormAttachment(_connTableTree, 5, SWT.BOTTOM);
        formData_3.left = new FormAttachment(_connTableTree, 0, SWT.LEFT);
        buttonComp.setLayoutData(formData_3);
        buttonComp.setLayout(new FormLayout());

        final Button addConnBtn = new Button(buttonComp, SWT.NONE);
        final FormData formData_4 = new FormData();
        formData_4.top = new FormAttachment(0, 5);
        formData_4.left = new FormAttachment(0, 5);
        addConnBtn.setLayoutData(formData_4);
        addConnBtn.setText(Messages.BindingEditor_add_type_binding_button);
        addConnBtn.addSelectionListener( new SelectionAdapter() 
        {
        	@SuppressWarnings("unchecked")
        	public void widgetSelected(SelectionEvent evt) 
        	{
                IType roleClass = null;
                
                AddTypeBindingDialog dlg =
                    AddTypeBindingDialog.create(parent.getShell(), teamType);
                if (AddTypeBindingDialog.OK == dlg.open()) 
                {
                    roleClass = dlg.getRoleType();
                }
                else
                {
                	return;
                }

                AST ast = _rootTeam.getAST();

                RoleTypeDeclaration role = ast.newRoleTypeDeclaration();
                role.setName(ast.newSimpleName(roleClass.getElementName()));
                role.setRole(true);
                try {
					int flags = roleClass.getFlags() & ~Modifier.ABSTRACT;
					role.modifiers().addAll(ast.newModifiers(flags));
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
                String qualifiedBaseTypeName = dlg.getBaseTypeName();
				String[] identifiers = qualifiedBaseTypeName.split("\\."); //$NON-NLS-1$
                
                // use the single name for playedBy:
                int len = identifiers.length;
                String singleName = identifiers[len-1];
				role.setBaseClassType(ast.newSimpleType(ast.newName(singleName)));
				BindingEditor.this._baseClassLookup.put(singleName, qualifiedBaseTypeName);
              
                // add an import using the qualified name:
                _rootTeam.bodyDeclarations().add(role);
                ImportDeclaration baseImport = ast.newImportDeclaration();
                baseImport.setBase(true);
                baseImport.setName(ast.newName(identifiers));
                root.imports().add(baseImport);

                refresh();
                _connTableViewer.setSelection(new StructuredSelection(role));
                bindingTableSelectionChanged();
            }
        });

        final Button remConnBtn = new Button(buttonComp, SWT.NONE);
        final FormData formData_5 = new FormData();
        formData_5.top = new FormAttachment(addConnBtn, 0, SWT.TOP);
        formData_5.left = new FormAttachment(addConnBtn, 5, SWT.RIGHT);
        remConnBtn.setLayoutData(formData_5);
        remConnBtn.setText(Messages.BindingEditor_remove_button);
        remConnBtn.addSelectionListener( new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent evt) 
			{
				removeElementsFromAST();
				refresh();
				bindConfig.resetLists();
			}
		});

        // TabFolder
        _tabFolder = new TabFolder(this, SWT.NONE);
        final FormData formData_1 = new FormData();
        formData_1.bottom = new FormAttachment(100, -5);
        formData_1.right = new FormAttachment(100, -5);
        formData_1.top = new FormAttachment(connDefGroup, 5, SWT.BOTTOM);
        formData_1.left = new FormAttachment(0, 5);
        _tabFolder.setLayoutData(formData_1);

        // method mapping tab item
        bindConfig = new BindingConfiguration(_tabFolder, SWT.NONE);
        bindConfig.setCurrentTeamForMethodFake(teamType);
        
        _methMapItem = new TabItem(_tabFolder, SWT.NONE);
        _methMapItem.setText(Messages.BindingEditor_method_binding_tab);
        _methMapItem.setControl(bindConfig);
        _methMapItems = new TabItem[] { _methMapItem };
        

        // parameter mapping tab item
        _callinMapConfig = new CallinMappingConfiguration(_tabFolder, SWT.NONE);
        _calloutMapConfig = new CalloutMappingConfiguration(_tabFolder, SWT.NONE);

        _paraMapItem = new TabItem(_tabFolder, SWT.NONE);
        _paraMapItem.setText(Messages.BindingEditor_param_mapping_tab);
        _paraMapItem.setControl(_callinMapConfig);
        _paraMapItems = new TabItem[] { _paraMapItem };
    }
    
	private void calculateRootNode(CompilationUnit cu) 
	{
	    // Note: we compare the source-start of the IType's name with the DOM node's simpleName's
	    // source-start, instead of using the type's start-position. This is because the IType's
	    // getSourceRange() method returns a different position than dom.TypeDeclaration.getStartPosition()
	    // depending on the type of comment before the type declaration (/** */ vs. /* */), so they
	    // didn't match.
	    
		try 
		{
			final TypeDeclaration[] teamDecl = new TypeDeclaration[1];
			final int teamPos = _team.getNameRange().getOffset();

			ASTVisitor visitor = new org.eclipse.jdt.internal.corext.dom.GenericVisitor() {
				public boolean visit(CompilationUnit node) {
					for (Iterator iter = node.types().iterator(); iter.hasNext();) {
						TypeDeclaration type = (TypeDeclaration) iter.next();
						if (!visit(type))
							return false;
					}
					return false;
				}
				
				public boolean visit(TypeDeclaration node) {
					int pos = node.getName().getStartPosition();
					if (pos == teamPos) {
						teamDecl[0] = node;
						return false;
					}

					TypeDeclaration[] memberTypes = node.getTypes();
					for (int i = 0; i < memberTypes.length; i++) {
						if (!visit(memberTypes[i]))
							return false;
					}
					
					return true;
				}
				
				public boolean visit(RoleTypeDeclaration node) {
					return visit ((TypeDeclaration) node);
				}

			};
			
			visitor.visit(cu);

			_rootTeam = teamDecl[0];
		} 
		catch (JavaModelException ex) {}
	}

	private void prepareParameterMappingConfiguration(
    		AbstractMethodMappingDeclaration methMap,
			ParameterMapping paraMap)
    {
        if (methMap == null)
        {
            _paraMapItem.setControl(null);
        }
		else if (methMap instanceof CallinMappingDeclaration)
        {
			_paraMapItem.setControl(_callinMapConfig);
			_callinMapConfig.setFocus(
                    paraMap,
					(CallinMappingDeclaration)methMap);
		}
        else if (methMap instanceof CalloutMappingDeclaration)
        {
			_paraMapItem.setControl(_calloutMapConfig);
			_calloutMapConfig.setFocus(
                    paraMap,
					(CalloutMappingDeclaration)methMap);
		}
	}
    
    private void removeElementsFromAST()
    {
		TreeItem[] selectedItems = _connTableTree.getSelection();
		ASTNode selectedNode = null;
	    
		for (int idx = 0; idx < selectedItems.length; idx++)
		{
			selectedNode = (ASTNode)selectedItems[idx].getData();
			
			if (selectedNode.getParent() instanceof TypeDeclaration)
			{
				removeElementFromAST(selectedNode);
			}
			else if(selectedNode.getParent() instanceof AbstractMethodMappingDeclaration)
			{
				removeElementFromAST((ParameterMapping)selectedNode);
			}
		}
    }

    /** Removes roles from teams and mappings from roles */
    private void removeElementFromAST(ASTNode selectedNode)
    {
    	TypeDeclaration parent = (TypeDeclaration)selectedNode.getParent();
		parent.bodyDeclarations().remove(selectedNode);
    }
    
    /** Removes ParameterMappings from callin or callout */
    private void removeElementFromAST(ParameterMapping selectedNode)
    {
        AbstractMethodMappingDeclaration abstractMethodMappingDeclaration = 
            (AbstractMethodMappingDeclaration)selectedNode.getParent();
        abstractMethodMappingDeclaration.getParameterMappings().remove(selectedNode);
    }

    protected void checkSubclass() {}
    
    private AbstractMethodMappingDeclaration getAbstractMethodMapping(ParameterMapping paraMap)
    {
        return (AbstractMethodMappingDeclaration)paraMap.getParent();
    }
    
    public void refresh()
    {
    	_connTableViewer.refresh();
    }

	public void bindingTableSelectionChanged() 
    {
		TreeItem[] selectedItems = _connTableTree.getSelection();
		ASTNode selectedNode = null;
		
		for (int idx = 0; idx < selectedItems.length; idx++)
		{
		    selectedNode = (ASTNode)selectedItems[idx].getData();
		    switch (selectedNode.getNodeType())
		    {
		        case ASTNode.ROLE_TYPE_DECLARATION:
		            RoleTypeDeclaration roleTypeDecl =
		                (RoleTypeDeclaration)selectedNode;

		            bindConfig.setFocusRole(roleTypeDecl, _team, true); 
		            prepareParameterMappingConfiguration(null, null);
		            _tabFolder.setSelection(_methMapItems);
		            break;
		        case ASTNode.CALLOUT_MAPPING_DECLARATION:
		            bindConfig.setCalloutMapping(selectedNode, _team);
		            prepareParameterMappingConfiguration(
		                    (AbstractMethodMappingDeclaration)selectedNode,
		                    null);
		            _tabFolder.setSelection(_methMapItems);
		            break;
		        case ASTNode.CALLIN_MAPPING_DECLARATION:
		        	bindConfig.setCallinMapping(selectedNode, _team);
		            prepareParameterMappingConfiguration(
		                (AbstractMethodMappingDeclaration)selectedNode,
		                null);
		            _tabFolder.setSelection(_methMapItems);
		            break;
		        case ASTNode.PARAMETER_MAPPING:
		            ParameterMapping paraMap =
		                (ParameterMapping)selectedNode;
		        
		            AbstractMethodMappingDeclaration methMap =
		                getAbstractMethodMapping(paraMap);
		            prepareParameterMappingConfiguration(methMap, paraMap);
		            
		            _tabFolder.setSelection(_paraMapItems);
		            break;
		        default:
		            break;
		    }
		}
	}

	public void methodBindingAdded(AbstractMethodMappingDeclaration mapping) 
	{
		_connTableViewer.expandToLevel(mapping.getParent(), 1);
		// Note(SH): do not select the new element, because this caused
		//           subsequent creation of method mappings to replace this method mapping.
		//           This problem annoyed us throughout all demos (Chicago) and tutorials (Erfurt, Bonn)!
		//_connTableViewer.setSelection(new StructuredSelection(mapping));
		bindingTableSelectionChanged();
	}
	
	private Name getRoleBaseClass(RoleTypeDeclaration element) {
        Name result = null;
    	Type baseType = element.getBaseClassType();
    	if(baseType != null) {
    		if (baseType.isParameterizedType())
         		baseType= ((ParameterizedType)baseType).getType();
        	if(baseType.isSimpleType())
        		result = ((SimpleType)baseType).getName();
        	else
        	if(baseType.isQualifiedType())
        		result = ((QualifiedType)baseType).getName();
        	else
        		assert false; // what's that?
    	}
        return result;
	}

}
