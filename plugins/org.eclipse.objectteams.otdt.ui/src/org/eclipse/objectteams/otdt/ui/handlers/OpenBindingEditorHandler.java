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
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleFileType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.ui.bindingeditor.BindingEditorDialog;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler responsible for the openBindingEditor command.
 * @author mosconi
 */
public class OpenBindingEditorHandler extends AbstractHandler {
	
	private IOTType currentTeam = null;
	
	public OpenBindingEditorHandler() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#setEnabled(java.lang.Object)
	 */
	@Override
	public void setEnabled(Object evaluationContext) {
		ISelection selection = (ISelection) HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_CURRENT_SELECTION_NAME);
		currentTeam = getRootTeam(selection);
		if (currentTeam != null)
			setBaseEnabled(true);
		else
			setBaseEnabled(false);
	}

	/**
	 * Opens the Binding Editor Dialog.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		new BindingEditorDialog(window.getShell(), currentTeam).open();
		return null;
	}
	
	/**
	 * Finds the outermost containing team for the given selection.
	 * @param selection The current selection (can be null).
	 * @return The best matching team for the selection, or null if no team found.
	 */
	protected IOTType getRootTeam(ISelection selection) {
		IOTType selectedTeam = null;
		if (!(selection instanceof IStructuredSelection)) 
			return null;			
		Object element = ((IStructuredSelection) selection).getFirstElement();
		if (!(element instanceof IJavaElement)) 
			return null;
		IJavaElement jElement = (IJavaElement) element;
		ICompilationUnit cu = (ICompilationUnit) jElement.getAncestor(IType.COMPILATION_UNIT);
		if (cu == null)
			return null;
		IType type = cu.findPrimaryType();
		IOTType otType = OTModelManager.getOTElement(type);
		if (otType == null) 
			return null;
		if (otType.isTeam())
			selectedTeam = otType;
		else if (otType instanceof IRoleFileType)
			selectedTeam = ((IRoleFileType)otType).getTeam();
		return selectedTeam;		
	}
}
