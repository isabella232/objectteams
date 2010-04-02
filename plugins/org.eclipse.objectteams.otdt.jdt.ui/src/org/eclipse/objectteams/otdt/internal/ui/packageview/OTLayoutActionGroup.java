/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTLayoutActionGroup.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.packageview;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.actions.MultiActionGroup;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.objectteams.otdt.internal.ui.Messages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class OTLayoutActionGroup extends MultiActionGroup 
{
	OTLayoutActionGroup(final PackageExplorerAdaptor adaptor, PartAdaptor<@adaptor> packageExplorer) {
		super(createActions(adaptor, packageExplorer), getSelectedState(packageExplorer));
	}

	/* (non-Javadoc)
	 * @see ActionGroup#fillActionBars(IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		contributeToViewMenu(actionBars.getMenuManager());
	}
	
	private void contributeToViewMenu(IMenuManager viewMenu) {
		viewMenu.add(new Separator());

		// Create layout sub menu
		
		IMenuManager layoutSubMenu= new MenuManager(Messages.OTLayoutActionGroup_MenuOTPresentations); 
		final String layoutGroupName= "layout"; //$NON-NLS-1$
		viewMenu.appendToGroup(layoutGroupName, layoutSubMenu);
//		viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));//$NON-NLS-1$		
		addActions(layoutSubMenu);
	}

	static int getSelectedState(PackageExplorerPart packageExplorer) {
		if (packageExplorer.isFlatLayout())
			return 0;
		else
			return 1;
	}
	
	static IAction[] createActions(final PackageExplorerAdaptor adaptor, PartAdaptor<@adaptor> packageExplorer) 
	{
		IAction[] actions = new IAction[2]; 
		actions[0]= new CallinFormattingAction(adaptor, packageExplorer, true);
		actions[0].setText(Messages.OTLayoutActionGroup_MenuShowCallinLabels);  
		actions[1]= new CallinFormattingAction(adaptor, packageExplorer, false);
		actions[1].setText(Messages.OTLayoutActionGroup_MenuDontShowCallinLabels); 

		actions[0].setChecked(true);
		return actions;
	}
}
@SuppressWarnings("restriction")
class CallinFormattingAction extends Action implements IAction 
{	
	final PackageExplorerAdaptor adaptor;
	PartAdaptor<@adaptor> packageExplorer;
	final boolean showLabel;
	
	CallinFormattingAction(final PackageExplorerAdaptor adaptor, PartAdaptor<@adaptor> packageExplorer, boolean showLabel) 
	{
		super("", AS_RADIO_BUTTON); //$NON-NLS-1$
		this.adaptor = adaptor;
		this.packageExplorer = packageExplorer;
		this.showLabel = showLabel;
		// FIXME(SH): 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.LAYOUT_FLAT_ACTION);
	}
	
	/*
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		packageExplorer.setCallinFormatting(this.showLabel);
	}
}
