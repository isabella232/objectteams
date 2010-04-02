/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BindingEditorDialog.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.bindingeditor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.actions.OrganizeImportsAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * The Main UI element of the binding editor.
 * 
 * Created on Feb 7, 2005
 * 
 * @author jwloka
 * @version $Id: BindingEditorDialog.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class BindingEditorDialog extends Dialog
{
	private String          _title;
	protected IType         _team;
	private BindingEditor   _bindingEditor;
	private CompilationUnit _root;
	private IDocument       _originalDocument;
	
    public BindingEditorDialog(Shell parentShell, IType teamType)
    {
        super(parentShell);
        super.setShellStyle(getShellStyle() | SWT.RESIZE);
        _title = Messages.BindingEditorDialog_dialog_title + teamType.getElementName();
        _team = teamType;
    }

	public int open() 
	{
        parseCurrentTeam();
        openResource((IFile)_team.getCompilationUnit().getResource());

		return super.open();
	}

	protected void configureShell(Shell shell) 
	{
		super.configureShell(shell);
		if (_title != null)
			shell.setText(_title);
	}
    
    protected Control createDialogArea(Composite parent)
    {
        
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FormLayout());
        
        _bindingEditor = new BindingEditor(container, SWT.NONE, _team, _root);
        final FormData formData = new FormData();
        formData.bottom = new FormAttachment(100, -5);
        formData.right = new FormAttachment(100, -5);
        formData.top = new FormAttachment(0, 5);
        formData.left = new FormAttachment(0, 5);
        _bindingEditor.setLayoutData(formData);
        return container;
    }

    protected void createButtonsForButtonBar(Composite parent)
    {
        createButton(
            parent,
            IDialogConstants.OK_ID,
            IDialogConstants.OK_LABEL,
            true);
        createButton(
            parent,
            IDialogConstants.CANCEL_ID,
            IDialogConstants.CANCEL_LABEL,
            false);
    }

    protected Point getInitialSize()
    {
    	Point calced = super.getInitialSize();
        return new Point(Math.max(calced.x, 881), calced.y);
    }
   
    @SuppressWarnings("unchecked")
	protected void okPressed()
    {
		setReturnCode(OK);
			
		IProgressMonitor monitor = new NullProgressMonitor();
		ICompilationUnit cu = _team.getCompilationUnit();
		Map<String, String> options = cu.getJavaProject().getOptions(true);
		// recognize OT/J syntax even in fragments not starting with "team":
		options.put(org.eclipse.jdt.internal.compiler.impl.CompilerOptions.OPTION_AllowScopedKeywords, 
				    org.eclipse.jdt.internal.compiler.impl.CompilerOptions.DISABLED);
		TextEdit edits = _root.rewrite(_originalDocument, options);
        	
		try 
		{
			edits.apply(_originalDocument);
			String newSource = _originalDocument.get();
			IBuffer buf = cu.getBuffer();
			buf.setContents(newSource);
			// TODO(jsv) use "organize imports" also for closed files, this version works only on open file  
			try 
			{
				IWorkbenchPage activePage = org.eclipse.jdt.internal.ui.JavaPlugin.getActivePage();
				IWorkbenchSite site = activePage.getActiveEditor().getEditorSite();
				OrganizeImportsAction organizeImportsAction = new OrganizeImportsAction(site);
				organizeImportsAction.run(cu);
			} 
			catch (NullPointerException ex) 
			{
				org.eclipse.jdt.internal.ui.JavaPlugin.log(ex);     
			}

			buf.save(monitor, false);
		} 
		catch (Exception ex) 
		{
			org.eclipse.jdt.internal.ui.JavaPlugin.log(ex);
		}
		close();
	}
    
    private void openResource(final IFile resource) 
    {
    	try 
    	{
    		IDE.openEditor(org.eclipse.jdt.internal.ui.JavaPlugin.getActivePage(), resource, true);
    	}
    	catch (PartInitException ex)
		{
    		org.eclipse.jdt.internal.ui.JavaPlugin.log(ex);
		}
	}
    
    private void parseCurrentTeam()
    {
        try 
        {
			_originalDocument = new Document(_team.getCompilationUnit().getSource());
			
			ASTParser parser = ASTParser.newParser(AST.JLS3);

            parser.setSource(_team.getCompilationUnit());
            parser.setResolveBindings(true);
            
            // tell the ASTConverter to include contained role files.
            // Note, that currently these role files cannot be modified by the binding editor (Trac #93).
            HashMap<String, String> options= new HashMap<String, String>(JavaCore.getOptions());
            options.put(JavaCore.AST_INCLUDES_ROLE_FILES, JavaCore.ENABLED);
            parser.setCompilerOptions(options);
            
			_root = (CompilationUnit)parser.createAST(null);
			_root.recordModifications();
		} 
        catch (JavaModelException ex) 
        {
			_originalDocument = null;
			org.eclipse.jdt.internal.ui.JavaPlugin.log(ex);
		}
    }
}
