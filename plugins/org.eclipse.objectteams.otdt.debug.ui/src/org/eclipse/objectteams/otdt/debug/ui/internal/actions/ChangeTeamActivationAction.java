/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ChangeTeamActivationAction.java 23432 2010-02-03 23:13:42Z stephan $
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.debug.ui.internal.actions;

import java.util.Iterator;

import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.objectteams.otdt.debug.TeamInstance;
import org.eclipse.objectteams.otdt.debug.ui.OTDebugImages;
import org.eclipse.objectteams.otdt.debug.ui.OTDebugUIPlugin;
import org.eclipse.objectteams.otdt.debug.ui.views.TeamView;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;

public class ChangeTeamActivationAction extends SelectionProviderAction {

	// selectors of methods to invoke:
	private static final String DEACTIVATE = "deactivate"; //$NON-NLS-1$
	private static final String ACTIVATE = "activate"; //$NON-NLS-1$
	// signatures of (de)activate() methods:
	private static final String SIGNATURE = "()V"; //$NON-NLS-1$
	private static final String SIGNATURE_THREAD_ARG = "(Ljava/lang/Thread;)V"; //$NON-NLS-1$
	// for constant org.objectteams.Team.ALL_THREADS:
	private static final String ALL_THREADS = "ALL_THREADS"; //$NON-NLS-1$

	protected IVariable fVariable;
    private TeamView fView;
    String selector;
    private boolean isActivate;
	
    /**
     * Creates a new ChangeTeamActivationAction for the given variables view
     * @param view the variables view in which this action will appear
     */
	public ChangeTeamActivationAction(TeamView view, boolean isActivate) {
		super(view.getViewer(), isActivate
									? ActionMessages.ChangeTeamActivationAction_activate_label
									: ActionMessages.ChangeTeamActivationAction_deactivate_label); 
		this.selector= isActivate ? ACTIVATE : DEACTIVATE;
		this.isActivate= isActivate;
		this.setEnabled(false);
		
		if (isActivate) {
			setDescription(ActionMessages.ChangeTeamActivationAction_activate_description); 
			setImageDescriptor(OTDebugImages.get(OTDebugImages.TEAM_ACTIVATED));
		} else {
			setDescription(ActionMessages.ChangeTeamActivationAction_deactivate_description); 
			setImageDescriptor(OTDebugImages.get(OTDebugImages.TEAM_INACTIVATED));
		}
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
			this,
			IDebugHelpContextIds.CHANGE_VALUE_ACTION);
		fView= view;
	}
	
	protected void doActionPerformed(Object element)
	{
		if (! (element instanceof TeamInstance))
			return;
		TeamInstance teamInstance= (TeamInstance)element;
		try {
			IValue value= teamInstance.getValue();
			if (value instanceof IJavaObject) {
				IJavaObject teamObject= (IJavaObject) value;
				String methodSignature = SIGNATURE; // default
				IJavaValue[] args= null;			// default
				IJavaThread thread= fView.getSelectedThread();
				if (thread == null) {
					// no thread selected means: (de)activate for all threads.
					methodSignature= SIGNATURE_THREAD_ARG;
					
					// create argument ALL_THREADS
					IJavaFieldVariable field= teamObject.getField(ALL_THREADS, true);
					args= new IJavaValue[]{(IJavaValue) field.getValue()};
					
					// search for a suspended thread:
					IDebugTarget target= teamObject.getDebugTarget();
					for (IThread tThread: target.getThreads()) {
						if (tThread.isSuspended()) {
							thread= (IJavaThread)tThread;
							break;
						}
					}
					if (thread == null) {
						String cause= ActionMessages.ChangeTeamActivationAction_error_no_thread_suspended;
						DebugUIPlugin.errorDialog(fView.getViewSite().getShell(), 
								ActionMessages.ChangeTeamActivationAction_error_title,
								cause,
								new Status(Status.ERROR, OTDebugUIPlugin.PLUGIN_ID, cause));
						return;
					}
				}
				teamObject.sendMessage(this.selector, methodSignature, args, thread, false);
				Viewer viewer = fView.getViewer();
				viewer.setSelection(viewer.getSelection()); // refresh action enablement
			}
		} catch (DebugException de) {
			DebugUIPlugin.errorDialog(fView.getViewSite().getShell(), 
					ActionMessages.ChangeTeamActivationAction_error_title,
					ActionMessages.ChangeTeamActivationAction_error_exception,
					de);	 
		}
	}
			
	/**
	 * Updates the enabled state of this action based
	 * on the selection
	 */
	protected void update(IStructuredSelection sel) {
		if (sel.size() > 1) {
			setEnabled(false); // can only activate one team at a time.
			return;
		}
		Iterator iter= sel.iterator();
		if (iter.hasNext()) {
			Object object= iter.next();
			if (object instanceof TeamInstance) {
				TeamInstance instance= (TeamInstance)object;
				if (   instance.isActiveFor(fView.getSelectedThread())
					!= isActivate) 
				{
					setEnabled(true);
					return;
				}
			}
		}
		setEnabled(false); // no team instance selected
	}

	/**
	 * @see IAction#run()
	 */
	public void run() {
		Iterator iterator= getStructuredSelection().iterator();
		doActionPerformed(iterator.next());
	}
	
	/**
	 * @see SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection sel) {
		update(sel);
	}
}
