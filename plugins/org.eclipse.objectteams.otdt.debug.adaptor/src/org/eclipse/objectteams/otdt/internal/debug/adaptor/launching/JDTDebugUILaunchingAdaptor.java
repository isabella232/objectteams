/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JDTDebugUILaunchingAdaptor.java 23456 2010-02-04 20:44:45Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.internal.debug.ui.jres.JREsComboBlock;
import org.eclipse.objectteams.otdt.debug.IOTLaunchConstants;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import base org.eclipse.jdt.debug.ui.launchConfigurations.JavaConnectTab;
import base org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;

/**
 * This team adds a new section to the JRE tab containing
 * <ul> 
 * <li>a checkbox "Enable OTRE"
 * <li>a checkbox "Use JPLIS (experimental)"
 * </ul>
 * If this box is enabled, launching will happen in OT/J mode, which is
 * managed by the {@link JDTLaunchingAdaptor}.
 * 
 * @author stephan
 * @since 1.2.1
 */
@SuppressWarnings("restriction")
public team class JDTDebugUILaunchingAdaptor {

	/**
	 * Adds the "use JPLIS" option to an OTREBlock, and also connects the OTREBlock to its base.
	 */
	protected class JavaJRETab extends OTREBlock playedBy JavaJRETab {

		Button _jplisToggleButton;
		boolean _useJPLIS = true;
		
		// === Imports (callout) : ===
		@SuppressWarnings("decapsulation")
		Control getJREControl() -> get JREsComboBlock fJREBlock
			with { result <- fJREBlock.getControl() }

		@SuppressWarnings("decapsulation")
		Button createCheckButton(Composite arg0, String arg1) -> Button createCheckButton(Composite arg0, String arg1);
		
		@SuppressWarnings("decapsulation")
		void setDirty(boolean dirty) -> void setDirty(boolean dirty);

		@SuppressWarnings("decapsulation")
		void updateLaunchConfigurationDialog() -> void updateLaunchConfigurationDialog();
		
		// build the GUI:
		Group createOTRESection(Composite parent) <- after void createControl(Composite parent);
		public Group createOTRESection(Composite parent) {
			Composite enclosingComposite = (Composite) this.getJREControl();
			Group group = super.createOTRESection(enclosingComposite, true/*useSWTFactory*/);
	        this._jplisToggleButton = createCheckButton(group, OTDTUIPlugin.getResourceString("OTJavaMainTab.jplis_checkbox_label")); //$NON-NLS-1$
	        this._jplisToggleButton.addSelectionListener(new SelectionAdapter() 
		        {
		            public void widgetSelected(SelectionEvent e)
		            {
		                JavaJRETab.this._useJPLIS = JavaJRETab.this._jplisToggleButton.getSelection();
		                setDirty(true);
		                updateLaunchConfigurationDialog();
		            }
		        });
			return group;
		}
		
		// read stored value:
		void initializeFrom(ILaunchConfiguration config) <- after void initializeFrom(ILaunchConfiguration config);
		
		protected void tryInitializeFrom(ILaunchConfiguration config, boolean hasOTJProject) throws CoreException {
			super.tryInitializeFrom(config, hasOTJProject);
			this._jplisToggleButton.setEnabled(hasOTJProject);
			this._useJPLIS = config.getAttribute(IOTLaunchConstants.ATTR_USE_JPLIS, this._useJPLIS);
			this._jplisToggleButton.setSelection(this._useJPLIS);
		}
		
		// apply value change:
		void performApply(ILaunchConfigurationWorkingCopy config) 
				<- after void performApply(ILaunchConfigurationWorkingCopy config);
		public void performApply(ILaunchConfigurationWorkingCopy config) {
	     	super.performApply(config);
	     	config.setAttribute(IOTLaunchConstants.ATTR_USE_JPLIS, this._useJPLIS);
	    }
	}
	
	/** Add an OTRE block to the "Connect" tab for remote debugging. */
	@SuppressWarnings("decapsulation")
	protected class JavaConnectTab extends OTREBlock playedBy JavaConnectTab {

		Control getControl() -> Control getControl();

		void createVerticalSpacer(Composite comp, int colSpan) -> void createVerticalSpacer(Composite comp, int colSpan);

		Button createCheckButton(Composite parent, String label) -> Button createCheckButton(Composite parent, String label);

		void setDirty(boolean dirty) -> void setDirty(boolean dirty);

		void updateLaunchConfigurationDialog() -> void updateLaunchConfigurationDialog();

		// build the GUI:
		Group createOTRESection(Composite parent) <- after void createControl(Composite parent);
		public Group createOTRESection(Composite parent) {
			Composite enclosingComposite = (Composite) this.getControl();
			createVerticalSpacer(enclosingComposite, 10);
			Group group = super.createOTRESection(enclosingComposite, true/*useSWTFactory*/);
			return group;
		}
		
		// hook the trigger for passing the ot-launch attribute:
		void performApply(ILaunchConfigurationWorkingCopy config) 
			<- after void performApply(ILaunchConfigurationWorkingCopy config);
	}
}
