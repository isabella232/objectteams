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
 * $Id: CalloutMappingConfiguration.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.bindingeditor;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccessSpec;
import org.eclipse.jdt.core.dom.MethodMappingElement;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.ParameterMapping;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTNodeCreator;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

/**
 * This is a configuration element for the
 * parameter mappings of a callout mapping.
 * 
 * A parameter mapping: expression "->" identifier  
 * A result mapping:      "result" "<-" expression 
 * 
 * @author jwloka
 */
public class CalloutMappingConfiguration extends Group
{
    // Method mapping declaration context
    private CalloutMappingDeclaration _callout;

    // parameter mapping
    private ListViewer _paraListViewer;
    private TextViewer _paraTextViewer;

    // result mapping
    private ListViewer _resListViewer;
    private TextViewer _resTextViewer;
    
    private BindingEditor _bindingEditor;
    private Label _methBindLabel;

    private final Object[] EMPTY = new Object[0];
    private static final String EMPTY_TEXT = ""; //$NON-NLS-1$

    
    class BaseMethodParameterContentProvider implements IStructuredContentProvider
    {
        public Object[] getElements(Object inputElement)
        {
            if (_callout == null)
            {
                return EMPTY;
            }

            java.util.List<String> result = new ArrayList<String>();
            MethodMappingElement baseElement = _callout.getBaseMappingElement(); 
            if (baseElement instanceof FieldAccessSpec) {
            	result.add(baseElement.getName().toString());
            } else if (baseElement instanceof MethodSpec) {
	            MethodSpec methSpec = (MethodSpec)baseElement;
	            for (Iterator paramIdx = methSpec.parameters().iterator(); paramIdx.hasNext();) {
	                SingleVariableDeclaration param = (SingleVariableDeclaration) paramIdx.next();
	                result.add(param.getName().toString());                
	            }
            }
            return result.toArray();
  
        }

        public void dispose()
        {}
        
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {}
    }

    
    class ResultContentProvider implements IStructuredContentProvider
    {
        public Object[] getElements(Object inputElement)
        {
            return new Object[] { "result" }; //$NON-NLS-1$
        }
        
        public void dispose()
        {}

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {}
    }

    
    public CalloutMappingConfiguration(Composite parent, int style)
    {
        super(parent, style);
        _bindingEditor = (BindingEditor)parent.getParent();
        setText(OTDTUIPlugin.getResourceString("CalloutMappingConfiguration_dialog_title")); //$NON-NLS-1$
        setLayout(new FormLayout());

        _methBindLabel = new Label(this, SWT.NONE);
        final FormData formData = new FormData();
        formData.right = new FormAttachment(100, -5);
        formData.top = new FormAttachment(0, 5);
        formData.left = new FormAttachment(0, 10);
        _methBindLabel.setLayoutData(formData);
        _methBindLabel.setText(OTDTUIPlugin.getResourceString("MethodMappingConfiguration_no_method_binding_selected")); //$NON-NLS-1$
        final Group paraMapGrp = new Group(this, SWT.NONE);
        paraMapGrp.setText(OTDTUIPlugin.getResourceString("MethodMappingConfiguration_param_mapping_group_title")); //$NON-NLS-1$
        final FormData formData_1 = new FormData();
        formData_1.bottom = new FormAttachment(50, 0);
        formData_1.right = new FormAttachment(100, -5);
        formData_1.top = new FormAttachment(0, 28);
        formData_1.left = new FormAttachment(0, 5);
        paraMapGrp.setLayoutData(formData_1);
        paraMapGrp.setLayout(new FormLayout());

        // left side, expression
        _paraTextViewer = new TextViewer(paraMapGrp, SWT.BORDER);
        _paraTextViewer.setDocument(new Document());
        final StyledText paraText = _paraTextViewer.getTextWidget();
        final FormData formData_5 = new FormData();
        formData_5.right = new FormAttachment(43, 0);
        formData_5.bottom = new FormAttachment(100, -5);
        formData_5.top = new FormAttachment(0, 5);
        formData_5.left = new FormAttachment(0, 5);
        paraText.setLayoutData(formData_5);
        _paraTextViewer.setInput(new Object());

        // right side, identiers
        _paraListViewer = new ListViewer(paraMapGrp, SWT.BORDER);
        _paraListViewer.setContentProvider(new BaseMethodParameterContentProvider());
        final List paraList = _paraListViewer.getList();
        final FormData formData_7 = new FormData();
        formData_7.bottom = new FormAttachment(100, -5);
        formData_7.right = new FormAttachment(100, -5);
        formData_7.top = new FormAttachment(0, 5);
        formData_7.left = new FormAttachment(59, 0);
        paraList.setLayoutData(formData_7);
        _paraListViewer.setInput(new Object());
        
        _paraListViewer.addSelectionChangedListener(
                new ISelectionChangedListener()
                {
                    public void selectionChanged(SelectionChangedEvent event)
                    {
                        if ( !_paraListViewer.getSelection().isEmpty() )
                        {
                            _resListViewer.setSelection(StructuredSelection.EMPTY);
                        }
                    }    
                
                }
        );
        
        
        
        final Composite paraBtnComp = new Composite(paraMapGrp, SWT.NONE);
        final FormData formData_6 = new FormData();
        formData_6.right = new FormAttachment(paraList, -5, SWT.LEFT);
        formData_6.bottom = new FormAttachment(100, -5);
        formData_6.top = new FormAttachment(paraText, 0, SWT.TOP);
        formData_6.left = new FormAttachment(paraText, 5, SWT.RIGHT);
        paraBtnComp.setLayoutData(formData_6);
        paraBtnComp.setLayout(new FormLayout());

        final Button paraMapBtn = new Button(paraBtnComp, SWT.NONE);
        final FormData formData_2 = new FormData();
        formData_2.right = new FormAttachment(100, -5);
        formData_2.top = new FormAttachment(34, 0);
        formData_2.left = new FormAttachment(0, 5);
        paraMapBtn.setLayoutData(formData_2);
        paraMapBtn.setText("->"); //$NON-NLS-1$


        final Button applyBtn = new Button(this, SWT.NONE);
        final FormData formData_11 = new FormData();
        formData_11.bottom = new FormAttachment(100, -5);
        formData_11.right = new FormAttachment(100, -5);
        applyBtn.setLayoutData(formData_11);
        applyBtn.setText(OTDTUIPlugin.getResourceString("MethodMappingConfiguration_apply_button_label")); //$NON-NLS-1$
        
        applyBtn.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent evt)
                {
                    applySelectedMapping();
                }
            }
        );
        
        final Group resMapGrp = new Group(this, SWT.NONE);
        resMapGrp.setText(OTDTUIPlugin.getResourceString("MethodMappingConfiguration_result_mapping_group_title")); //$NON-NLS-1$
        final FormData formData_4 = new FormData();
        formData_4.bottom = new FormAttachment(applyBtn, -5, SWT.TOP);
        formData_4.right = new FormAttachment(100, -5);
        formData_4.top = new FormAttachment(paraMapGrp, 5, SWT.BOTTOM);
        formData_4.left = new FormAttachment(0, 5);
        resMapGrp.setLayoutData(formData_4);
        resMapGrp.setLayout(new FormLayout());

        _resListViewer = new ListViewer(resMapGrp, SWT.BORDER);
        _resListViewer.setContentProvider(new ResultContentProvider());
        final List resList = _resListViewer.getList();
        final FormData formData_8 = new FormData();
        formData_8.bottom = new FormAttachment(100, -5);
        formData_8.right = new FormAttachment(43, 0);
        formData_8.top = new FormAttachment(0, 5);
        formData_8.left = new FormAttachment(0, 5);
        resList.setLayoutData(formData_8);
        _resListViewer.setInput(new Object());

        _resListViewer.addSelectionChangedListener(
                new ISelectionChangedListener()
                {
                    public void selectionChanged(SelectionChangedEvent event)
                    {
                        if ( !_resListViewer.getSelection().isEmpty() )
                        {
                            _paraListViewer.setSelection(StructuredSelection.EMPTY);
                        }
                    }    
                
                }
        );

        _resTextViewer = new TextViewer(resMapGrp, SWT.BORDER);
        final StyledText resText = _resTextViewer.getTextWidget();
        final FormData formData_10 = new FormData();
        formData_10.bottom = new FormAttachment(100, -5);
        formData_10.right = new FormAttachment(100, -5);
        formData_10.top = new FormAttachment(0, 5);
        formData_10.left = new FormAttachment(59, 0);
        resText.setLayoutData(formData_10);
        _resTextViewer.setInput(new Object());

        final Composite resBtnComp = new Composite(resMapGrp, SWT.NONE);
        final FormData formData_3 = new FormData();
        formData_3.right = new FormAttachment(resText, -5, SWT.LEFT);
        formData_3.bottom = new FormAttachment(100, -5);
        formData_3.top = new FormAttachment(resText, 0, SWT.TOP);
        formData_3.left = new FormAttachment(resList, 5, SWT.RIGHT);
        resBtnComp.setLayoutData(formData_3);
        resBtnComp.setLayout(new FormLayout());

        final Button resMapBtn = new Button(resBtnComp, SWT.NONE);
        final FormData formData_9 = new FormData();
        formData_9.right = new FormAttachment(100, -5);
        formData_9.top = new FormAttachment(0, 5);
        formData_9.left = new FormAttachment(0, 5);
        resMapBtn.setLayoutData(formData_9);
        resMapBtn.setText("<-"); //$NON-NLS-1$
        //
    }

    public void dispose()
    {
        super.dispose();
    }

    protected void checkSubclass()
    {}
    


    private void applySelectedMapping()
    {
        IStructuredSelection selection = null;
        String identifier = null;
        String expr = null;

        selection = (IStructuredSelection)_paraListViewer.getSelection();
        if (!selection.isEmpty())
        {
            identifier = (String)selection.getFirstElement();
            expr = _paraTextViewer.getDocument().get();
            if (!expr.equals(EMPTY_TEXT))
            {
                updateParameterMapping(identifier, expr);
            }
        }

        selection = (IStructuredSelection)_resListViewer.getSelection();
        if (!selection.isEmpty())
        {
            identifier = (String)selection.getFirstElement();
            expr = _resTextViewer.getDocument().get();
            if (!expr.equals(EMPTY_TEXT))
            {
                updateParameterMapping(identifier, expr);
            }
        }

        _bindingEditor.refresh();
    }

        
    protected void setFocus(ParameterMapping paraMap, CalloutMappingDeclaration callout)
    {
        setMethodBinding(callout);
        setParameterMapping(paraMap);
    }
    
    protected void setParameterMapping(ParameterMapping paraMap)
    {
        if (paraMap == null)
        {
            _resTextViewer.setDocument(new Document(EMPTY_TEXT));
            _paraTextViewer.setDocument(new Document(EMPTY_TEXT));
            clearSelections();
        	return;
        }
        
        String identifier = paraMap.getIdentifier().getIdentifier();
        ASTNode expr = paraMap.getExpression();
        if (paraMap.hasResultFlag())
        {
            _paraListViewer.setSelection(StructuredSelection.EMPTY);
            _resListViewer.setSelection(new StructuredSelection(identifier));
            _resTextViewer.setDocument(new Document(expr.toString()));
        }
        else
        {
            _resListViewer.setSelection(StructuredSelection.EMPTY);
            _paraListViewer.setSelection(new StructuredSelection(identifier));
            _paraTextViewer.setDocument(new Document(expr.toString()));
        }
    }
    
    protected void setMethodBinding(CalloutMappingDeclaration callout)
    {
        _callout = callout;
        _methBindLabel.setText(
                _callout.getRoleMappingElement().toString()
                + (_callout.isCalloutOverride() ? "=>" : "->") //$NON-NLS-1$ //$NON-NLS-2$
                + _callout.getBaseMappingElement().toString()
                );
        _paraListViewer.refresh();
    }
    
    @SuppressWarnings("unchecked") // AST does not use generics :(
	protected void updateParameterMapping(String identifier, String expr)
    {
        ParameterMapping paraMap = null;
        Expression exprNode = ASTNodeCreator.createExpression(_callout.getAST(), expr);

        // if parsing of expr fails, ignore this parameter mapping 
        if (exprNode == null)
        {
            return;
        }
        
        java.util.List<ParameterMapping> paraMaps = _callout.getParameterMappings();
        for (Iterator<ParameterMapping> mapIdx = paraMaps.iterator(); mapIdx.hasNext();)
        {
            ParameterMapping mapping = mapIdx.next();
            if (identifier.equals(mapping.getIdentifier()))
            {
                paraMap = mapping;
                break;
            }
            
        }
        
        if (paraMap == null)
        {
            paraMap = _callout.getAST().newParameterMapping();
            paraMaps.add(paraMap);    
        }
        
        paraMap.setIdentifier(_callout.getAST().newSimpleName(identifier));
        if (identifier.equals("result")) //$NON-NLS-1$
        {
            paraMap.setDirection("<-"); //$NON-NLS-1$
            paraMap.setResultFlag(true);
        }
        else
        {
            paraMap.setDirection("->"); //$NON-NLS-1$
            paraMap.setResultFlag(false);
        }
        
        paraMap.setExpression(exprNode);
    }
    
    
    public void clearSelections()
    {
        _resListViewer.setSelection(StructuredSelection.EMPTY);
        _paraListViewer.setSelection(StructuredSelection.EMPTY);
        _resTextViewer.setSelection(StructuredSelection.EMPTY);
        _paraTextViewer.setSelection(StructuredSelection.EMPTY);
    }
    
}
