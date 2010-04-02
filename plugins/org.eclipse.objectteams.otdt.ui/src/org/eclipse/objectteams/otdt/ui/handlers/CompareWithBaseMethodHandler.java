/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009, Stephan Herrmann.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.ui.OTDTUIMessages;
import org.eclipse.objectteams.otdt.internal.ui.compare.CompareBoundMethodsEditorInput;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.synchronize.SaveableCompareEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * This handler implements the "Compare with ... Bound base method" command.
 * 
 * @author stephan
 * @since 1.4.0
 */
public class CompareWithBaseMethodHandler extends AbstractHandler implements IStructuredSelection {

	IMethod roleMethod;
	IMethod baseMethod;
	ICallinMapping callinMapping;
	private IStructuredSelection selection;
	private IWorkbenchPage page;
	
	// === Implement IStructuredSelection: ===
	public boolean isEmpty() { return false; }

	public List toList() {
		List<IMethod> result = new ArrayList<IMethod>(2);
		result.add(roleMethod);
		result.add(baseMethod);
		return result;
	}

	public Object[] toArray() {
		return new Object[]{roleMethod, baseMethod};
	}

	public int size() { return 2; }

	public Iterator iterator() { return null; } // unused method

	public Object getFirstElement() { return null; } // unused method
	// === End IStructuredSelection ===
	

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#setEnabled(java.lang.Object)
	 */
	@Override
	public void setEnabled(Object evaluationContext)
	{	
		// fetch UI-context:
		IWorkbenchSite site = (IWorkbenchSite) HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_SITE_NAME);
		if (site == null) 
			return; // happens on workbench shutdown
		this.page = site.getPage();
		
		// initialize roleMethod and/or callinMapping from current selection:
		this.roleMethod = null;
		this.callinMapping = null;
		ISelection sel = (ISelection) HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (sel instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) sel;
			if (this.selection.size() == 1) {
				Object element = this.selection.getFirstElement();
				if (element instanceof IMethod) {
					IMethod method = (IMethod) element;
					try {
						if ((method.getFlags() & ExtraCompilerModifiers.AccCallin) != 0) {
							this.roleMethod = method; 
							setBaseEnabled(true);
							return;
						}
					} catch (JavaModelException e) {
						// nop, can't read method.getFlags();
					}
				} else if (element instanceof ICallinMapping) {
					this.callinMapping = (ICallinMapping) element;
					setBaseEnabled(true);
					return;
				}
			}
		}
		setBaseEnabled(false);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.baseMethod = null;
		if (this.roleMethod != null && this.callinMapping == null) {
			// find a callin mapping by which this role method is bound:
			IType type = (IType) roleMethod.getParent();
			IRoleType roleType = (IRoleType) OTModelManager.getOTElement(type);
			for (IMethodMapping mapping : roleType.getMethodMappings(IRoleType.CALLINS)) {
				if (mapping.getRoleMethod().equals(this.roleMethod))
					if (this.callinMapping == null) {
						this.callinMapping = (ICallinMapping)mapping;
					} else {
		                MessageDialog.openError(null, // shell 
        						OTDTUIMessages.CompareWithBaseMethodAction_errorTitle,
        						OTDTUIMessages.CompareWithBaseMethodAction_ambiguousBindingsError);
		                return null;
					}
			}
		}
		if (this.callinMapping != null) {
			// find the methods bound by this callin mapping:
			this.roleMethod = this.callinMapping.getRoleMethod();
			IMethod[] baseMethods;
			try {
				baseMethods = this.callinMapping.getBoundBaseMethods();
			} catch (JavaModelException e) {
				throw new ExecutionException(e.getMessage());
			}
			if (baseMethods.length > 1) {
                MessageDialog.openError(null, // shell 
                				OTDTUIMessages.CompareWithBaseMethodAction_errorTitle,
                				OTDTUIMessages.CompareWithBaseMethodAction_multipleBaseMethodsError);
				return null;
			}
			this.baseMethod = baseMethods[0];
		}
		
		if (this.roleMethod != null && this.baseMethod != null)
			openCompareEditor();
		
		return null;
	}

	void openCompareEditor() {
	    try {
			CompareEditorInput input = new CompareBoundMethodsEditorInput(this.roleMethod, this.baseMethod, this.page);
			IWorkbenchPage workBenchPage = this.page;
			IEditorPart editor = findReusableCompareEditor(input, workBenchPage);
			if (editor != null) {
				IEditorInput otherInput = editor.getEditorInput();
				if (otherInput.equals(input)) {
					// simply provide focus to editor
					if (OpenStrategy.activateOnOpen())
						workBenchPage.activate(editor);
					else
						workBenchPage.bringToTop(editor);
				} else {
					// if editor is currently not open on that input either re-use
					// existing
					CompareUI.reuseCompareEditor(input, (IReusableEditor) editor);
					if (OpenStrategy.activateOnOpen())
						workBenchPage.activate(editor);
					else
						workBenchPage.bringToTop(editor);
				}
			} else {
				CompareUI.openCompareEditor(input, OpenStrategy.activateOnOpen());
			}
		} catch (CoreException ce) {
			OTDTUIPlugin.log(ce);
		}
	}

	/**
	 * Returns an editor that can be re-used. An open compare editor that
	 * has un-saved changes cannot be re-used.
	 * @param input the input being opened
	 * @param page 
	 * @return an EditorPart or <code>null</code> if none can be found
	 */
	IEditorPart findReusableCompareEditor(CompareEditorInput input, IWorkbenchPage page) {
		IEditorReference[] editorRefs = page.getEditorReferences();
		// first loop looking for an editor with the same input
		for (int i = 0; i < editorRefs.length; i++) {
			IEditorPart part = editorRefs[i].getEditor(false);
			if (part != null
					&& (part.getEditorInput() instanceof CompareBoundMethodsEditorInput)
					&& part instanceof IReusableEditor
					&& part.getEditorInput().equals(input)) {
				return part;
			}
		}

		// if none found and "Reuse open compare editors" preference is on use
		// a non-dirty editor
		if (isReuseOpenEditor()) {
			for (int i = 0; i < editorRefs.length; i++) {
				IEditorPart part = editorRefs[i].getEditor(false);
				if (part != null
						&& (part.getEditorInput() instanceof SaveableCompareEditorInput)
						&& part instanceof IReusableEditor && !part.isDirty()) {
					return part;
				}
			}
		}

		// no re-usable editor found
		return null;
	}

	boolean isReuseOpenEditor() {
		return TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.REUSE_OPEN_COMPARE_EDITOR);
	}
}
