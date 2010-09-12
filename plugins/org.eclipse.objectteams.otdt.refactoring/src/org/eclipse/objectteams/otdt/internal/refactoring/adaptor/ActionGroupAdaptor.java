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
package org.eclipse.objectteams.otdt.internal.refactoring.adaptor;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.actions.InlineAction;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings.extractcallin.ExtractCallinAction;
import org.eclipse.objectteams.otdt.internal.refactoring.otrefactorings.inlinecallin.InlineCallinAction;
import org.eclipse.ui.IWorkbenchSite;

import base org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import base org.eclipse.jdt.ui.actions.RefactorActionGroup;

/**
 * @author stephan
 *
 */
@SuppressWarnings("restriction")
public team class ActionGroupAdaptor 
{
	public static final String INLINE_CALLIN = "org.eclipse.objectteams.otdt.refactoring.inline.callin"; //$NON-NLS-1$
	public static final String EXTRACT_CALLIN = "org.eclipse.objectteams.otdt.refactoring.extract.callin"; //$NON-NLS-1$
	
	protected team class CUEditorAdaptor playedBy CompilationUnitEditor 
	{
		createActions <- replace createActions;
		callin void createActions() {
			within (this)
				base.createActions();
		}
		
		// workaround for broken in-place lowering
		JavaEditor getJavaEditor() { return this; }
		
		/** Only active during createActions() - nested in order to pass down the editor instance. */
		protected class ActionGroupEditorTrigger playedBy RefactorActionGroup 
		{
			void initOTActions(SelectionDispatchAction action, ISelection selection)
			<- after void initAction(SelectionDispatchAction action, ISelection selection, String actionDefinitionId)
			base when (action instanceof InlineAction);
			
			private void initOTActions(SelectionDispatchAction action, ISelection selection) {
				new ActionGroup(this, getJavaEditor(), selection);
			}
		}
	}
	
	/** This trigger is globally active to register OT refactoring actions to the global refactoring menu. */
	protected class ActionGroupGlobalTrigger playedBy RefactorActionGroup 
	{
		void initOTActions(SelectionDispatchAction action, ISelectionProvider provider, ISelectionProvider specialProvider, ISelection selection, String actionDefinitionId)
		<- after void initUpdatingAction(SelectionDispatchAction action, ISelectionProvider provider, ISelectionProvider specialProvider, ISelection selection, String actionDefinitionId)
		base when (action instanceof InlineAction);
		
		private void initOTActions(SelectionDispatchAction action, ISelectionProvider provider, ISelectionProvider specialProvider, ISelection selection, String actionDefinitionId) {
			new ActionGroup(this, provider, specialProvider, selection);			
		}
	}
	
	/** This role finally adapts all registered refactoring action groups. */
	protected class ActionGroup playedBy RefactorActionGroup 
		base when (ActionGroupAdaptor.this.hasRole(base, ActionGroup.class))
	{
		@SuppressWarnings("decapsulation")
		boolean getFBinary() -> get boolean fBinary;

		@SuppressWarnings("decapsulation")
		IWorkbenchSite getFSite() -> get IWorkbenchSite fSite;

		@SuppressWarnings("decapsulation")
		void initUpdatingAction(SelectionDispatchAction action, ISelectionProvider provider, ISelectionProvider specialProvider, ISelection selection, String actionDefinitionId)
		-> void initUpdatingAction(SelectionDispatchAction action, ISelectionProvider provider, ISelectionProvider specialProvider, ISelection selection, String actionDefinitionId);
		
		SelectionDispatchAction inlineCallinAction;
		SelectionDispatchAction extractCallinAction;
		
		protected ActionGroup(RefactorActionGroup group, JavaEditor editor, ISelection selection) {
			this(group);

			this.inlineCallinAction = new InlineCallinAction().new SelectionDispatchAction(editor);
			initUpdatingAction(this.inlineCallinAction, null, null, selection, INLINE_CALLIN);
			editor.setAction("InlineCallin", this.inlineCallinAction); //$NON-NLS-1$
			
			this.extractCallinAction = new ExtractCallinAction().new SelectionDispatchAction(editor);
			initUpdatingAction(this.extractCallinAction, null, null, selection, EXTRACT_CALLIN);
			editor.setAction("ExtractCallin", this.extractCallinAction); //$NON-NLS-1$
		}

		protected ActionGroup(RefactorActionGroup group, ISelectionProvider provider, ISelectionProvider specialProvider,ISelection selection) 
		{
			this(group);

			this.inlineCallinAction = new InlineCallinAction().new SelectionDispatchAction(getFSite());
			initUpdatingAction(this.inlineCallinAction, provider, specialProvider, selection, INLINE_CALLIN);
			
			this.extractCallinAction = new ExtractCallinAction().new SelectionDispatchAction(getFSite());
			initUpdatingAction(this.extractCallinAction, provider, specialProvider, selection, EXTRACT_CALLIN);
		}

		void addAction(IMenuManager menu, IAction action) 
		<- after int addAction(IMenuManager menu, IAction action)
		base when (action instanceof InlineAction);
		
		
		private void addAction(IMenuManager menu, IAction action) {
			if (this.inlineCallinAction != null)
				menu.add(this.inlineCallinAction);
			if (this.extractCallinAction != null)
				menu.add(this.extractCallinAction);
		}
		
		void disposeAction(ISelectionChangedListener action, ISelectionProvider provider) 
		<- after void disposeAction(ISelectionChangedListener action, ISelectionProvider provider)
		base when (action instanceof InlineAction);
		
		
		private void disposeAction(ISelectionChangedListener action, ISelectionProvider provider) {
			if (this.inlineCallinAction != null)
				provider.removeSelectionChangedListener(this.inlineCallinAction);
			if (this.extractCallinAction != null)
				provider.removeSelectionChangedListener(this.extractCallinAction);
		}			
	}
}
