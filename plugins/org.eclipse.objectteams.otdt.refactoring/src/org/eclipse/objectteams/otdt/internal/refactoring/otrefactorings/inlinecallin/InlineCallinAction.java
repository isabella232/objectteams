/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Johannes Gebauer and Stephan Herrmann.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: InlineCallinAction.java 23473 2010-02-05 19:46:08Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * 		Johannes Gebauer - Initial API and implementation
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings.inlinecallin;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings.AbstractCallinRefactoringAction;

/**
 * Action classes for the inline callin refactoring:
 * <ul>
 * <li>The toplevel class provides the action to the main menu bar (registered via plugin.xml).</li>
 * <li>Inherited nested class {@link SelectionDispatchAction} provides the action to context menus of all views.</li>
 * <li>Nested class {@link InlineCallinActionCommon} implements the shared stateful part of both actions.</li>
 * </ul>
 */
@SuppressWarnings("restriction")
public class InlineCallinAction extends AbstractCallinRefactoringAction implements IWorkbenchWindowActionDelegate 
{	
	/** 
	 * Fill inherited class {@link CallinRefactoringActionCommon} with details.
	 */
	class InlineCallinActionCommon extends CallinRefactoringActionCommon
	{
		InlineCallinActionCommon(JavaEditor editor) {
			super(editor);
		}
		
		InlineCallinActionCommon(IWorkbenchWindow window) {
			super(window);
		}

		protected void filterElements(Object[] elements) {
			this.fJavaElement = null;
			if (elements == null || elements.length != 1)
				return;
			else if (elements[0] instanceof IMethod)
				this.fJavaElement = (IMethod) elements[0];
			else if (elements[0] instanceof ICallinMapping)
				this.fJavaElement = ((ICallinMapping)elements[0]).getRoleMethod();
		}

		protected void doRun() {
			if (fJavaElement != null && fWindow != null) {
				try {
					new RefactoringWizardOpenOperation(
							new InlineCallinWizard(
									new InlineCallinRefactoring((IMethod)fJavaElement), 
									"Inline Callin"
							)
					).run(fWindow.getShell(), "Inline Callin");
				} catch (InterruptedException exception) {
					// Do nothing
				}
			} else {
				MessageDialog.openInformation(null, "Inline Callin", "Operation unavailable on the current selection. Select a callin-bound role method or a callin method binding");
			}		
		}
	}
	
	
	// =========== Start InlineCallinAction ===========
	
	public InlineCallinAction () {
		super("Inline Callin ...", "Inline a callin-bound role meth.");
	}

	protected CallinRefactoringActionCommon createRefactoringActionCommon(IWorkbenchWindow window) {
		return new InlineCallinActionCommon(window);
	}

	protected CallinRefactoringActionCommon createRefactoringActionCommon(JavaEditor editor) {
		return new InlineCallinActionCommon(editor);
	}
}