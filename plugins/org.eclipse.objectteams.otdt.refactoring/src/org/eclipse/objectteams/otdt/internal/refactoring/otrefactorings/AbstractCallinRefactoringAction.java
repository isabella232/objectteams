/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Stephan Herrmann.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: InlineCallinAction.java 23045 2009-11-15 21:17:24Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaTextSelection;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Common part of refactoring actions affecting calling bindings.
 * 
 * @author stephan
 * @since 1.4.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractCallinRefactoringAction extends Action {

	/** 
	 * This class defines the common stateful part of {@link AbstractCallinRefactoringAction} and 
	 * {@link AbstractCallinRefactoringAction.SelectionDispatchAction}. 
	 */
	protected abstract class CallinRefactoringActionCommon extends SelectionActionCommon
	{
		public CallinRefactoringActionCommon(JavaEditor editor) {
			super(editor);
		}
		
		public CallinRefactoringActionCommon(IWorkbenchWindow window) {
			super(window);
		}

		public boolean isRefactoringAvailable() throws JavaModelException {
			if (   fJavaElement != null 
			  	&& fJavaElement.exists() 
			  	&& fJavaElement.isStructureKnown()
			  	&& fJavaElement instanceof IMethod) 
			{
				IMethod method = (IMethod)fJavaElement;
				return    !method.isConstructor() 
					   && !method.getDeclaringType().isAnnotation();				
			}
			return false;
		}
	}
	
	/** Provide the respective refactoring action to view context menus (editor, package explorer, outline ...) */
	public class SelectionDispatchAction extends org.eclipse.jdt.ui.actions.SelectionDispatchAction 
	{	
		CallinRefactoringActionCommon common;
		
		public SelectionDispatchAction(IWorkbenchSite site) {
			super(site);
			common = createRefactoringActionCommon(site.getWorkbenchWindow());
			init();
		}
		
		public SelectionDispatchAction(JavaEditor editor) {
			super(editor.getEditorSite());
			common = createRefactoringActionCommon(editor);
			init();
		}

		void init() {
			setText(commandName);
			setToolTipText(commandTooltip);
			setEnabled(true); // further checking in run().
		}

		@Override
		public void selectionChanged(JavaTextSelection javaTextSelection) {
			try {
				common.selectionChanged(javaTextSelection);
				setEnabled(common.isRefactoringAvailable());
			} catch (CoreException ce) {
				setEnabled(false);
			}
		}
		
		@Override
		public void selectionChanged(IStructuredSelection selection) {
			try {
				common.selectionChanged(selection);
				setEnabled(common.isRefactoringAvailable());
			} catch (JavaModelException e) {
				setEnabled(false);
			}
		}
		
		@Override
		public void selectionChanged(ITextSelection selection) {
			// ignore useless selection, let above methods prevail; further checks in run().
		}
	
		@Override
		public void run() {
			common.run();
		}
	}
	
	// =========== Start AbstractCallinRefactoringAction ===========

	CallinRefactoringActionCommon common;
	
	final protected String commandTooltip;
	final protected String commandName;
	
	protected AbstractCallinRefactoringAction(String commandName, String commandTooltip) {
		this.commandName = commandName;
		this.commandTooltip = commandTooltip;
	}

	public void init(IWorkbenchWindow window) {
		common = createRefactoringActionCommon(window);
	}

	abstract protected CallinRefactoringActionCommon createRefactoringActionCommon(IWorkbenchWindow window);
	abstract protected CallinRefactoringActionCommon createRefactoringActionCommon(JavaEditor editor);

	public void dispose() {
		// Do nothing
	}

	public void selectionChanged(IAction action, ISelection selection) {
		try {
			if (selection instanceof IStructuredSelection) {
				common.selectionChanged((IStructuredSelection)selection);
				action.setEnabled(common.isRefactoringAvailable());
			} else if (selection instanceof JavaTextSelection) {
				common.selectionChanged((JavaTextSelection) selection);
				action.setEnabled(common.isRefactoringAvailable());
			} else {
				common.clearJavaElement();
				action.setEnabled(true); // further checks in run().
			}
		} catch (JavaModelException exception) {
			action.setEnabled(false);
		}
	}

	public void run(IAction action) {
		common.run();
	}
}
