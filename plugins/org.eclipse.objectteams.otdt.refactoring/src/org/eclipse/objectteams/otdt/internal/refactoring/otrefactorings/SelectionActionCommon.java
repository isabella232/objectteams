/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Stephan Herrmann.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaTextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Common super class for actions that depend on the selection and need to be provided by several mechanisms.
 * @author stephan
 */
@SuppressWarnings("restriction")
public abstract class SelectionActionCommon {

	protected IJavaElement fJavaElement;
	protected JavaEditor fEditor;
	protected IWorkbenchWindow fWindow;

	public SelectionActionCommon(JavaEditor editor) {
		this.fEditor = editor;
	}
	
	public SelectionActionCommon(IWorkbenchWindow window) {
		this.fWindow = window;
	}
	
	public void selectionChanged(IStructuredSelection selection) {
		Object[] elements = selection.toArray();
		filterElements(elements);
	}

	public void selectionChanged(JavaTextSelection javaTextSelection) throws JavaModelException {
		IJavaElement[] elements= javaTextSelection.resolveElementAtOffset();
		filterElements(elements);
	}

	/** 
	 * Given some selected elements figure out whether this is a valid selection for the current action,
	 * and store the java element that {@link #run()} should operate on in the field {@link #fJavaElement}.
	 * 
	 * @param elements the elements of either a structured selection, a java text selection or 
	 * 		the result of {@link SelectionConverter#codeResolve(JavaEditor)}.
	 */
	protected abstract void filterElements(Object[] elements);

	public void clearJavaElement() {
		this.fJavaElement = null;
	}
	
	/** Finish initialization and trigger running the action. */
	public void run() {
		prepareRun();
		doRun();
	}
	
	/** This method performs those operations that are to costly to do during selectionChange. */
	protected void prepareRun() {
		// try to fill in missing info:
		if (this.fJavaElement == null) {
			// fetch the method from the editor:
			JavaEditor javaEditor = this.fEditor;
			if (javaEditor == null) 
				try {
					javaEditor = (JavaEditor)this.fWindow.getActivePage().getActiveEditor();
				} catch (ClassCastException cce) { /* nop */ }
			if (javaEditor != null)
				try {
					IJavaElement[] elements = SelectionConverter.codeResolve(javaEditor);
					filterElements(elements);
				} catch (JavaModelException e) {
					this.fJavaElement = null;
				}
		}
		if (this.fWindow == null && this.fEditor != null)
			// fetch the window from the editor:
			this.fWindow = this.fEditor.getSite().getWorkbenchWindow();
	}

	/** 
	 * Implementation for running the refactoring action.
	 */
	protected abstract void doRun();
}
