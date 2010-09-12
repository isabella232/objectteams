/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ShowOTInternalVariablesAction.java 23456 2010-02-04 20:44:45Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor.actions;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jdt.internal.debug.ui.actions.ToggleBooleanPreferenceAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.objectteams.otdt.internal.debug.adaptor.OTDebugAdaptorPlugin;
import org.eclipse.objectteams.otdt.internal.debug.adaptor.PreferenceInitializer;

/**
 * Toggle action for filtering OT-Internal variables (starting with "_OT$").
 * This action can be used by different views (currently: TeamView and VariablesView).
 * 
 * @author stephan
 * @since 1.2.0
 */
@SuppressWarnings("restriction")
public class ShowOTInternalVariablesAction extends ToggleBooleanPreferenceAction {

	public ShowOTInternalVariablesAction() {
		super();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.ViewFilterAction#getPreferenceKey()
	 */
	protected String getPreferenceKey() {
		return PreferenceInitializer.PREF_SHOW_OTINTERNAL_VARIABLES; 
	}
	
	protected IPreferenceStore getPreferenceStore() {
		return OTDebugAdaptorPlugin.getDefault().getPreferenceStore();
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.ToggleBooleanPreferenceAction#getViewKey()
	 */
	protected String getViewKey() {
		return getCompositeKey();
	}

	public static boolean includeOTInternal(IPresentationContext context){
		IPreferenceStore store = OTDebugAdaptorPlugin.getDefault().getPreferenceStore();
		String key = context.getId() + "." + PreferenceInitializer.PREF_SHOW_OTINTERNAL_VARIABLES; //$NON-NLS-1$
		return store.getBoolean(key);
	}
}
