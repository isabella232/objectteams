/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: LinkedModeAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.assist;

import org.eclipse.jface.text.link.ILinkedModeListener;
import base org.eclipse.jface.text.link.LinkedModeUI;

/**
 * This team observes the LinkeModeUI, which is responsible for editing
 * linked correction proposals.
 * The purpose of this observation is to leave linked mode before applying
 * the swap-precedences rewrite, which would break existing linkage.
 * 
 * @author stephan
 */
public team class LinkedModeAdaptor {
	
	public static LinkedModeAdaptor instance;

	private LinkedModeUI ui;
	
	public LinkedModeAdaptor() {
		instance = this;
	}

	/** This role provides access to the active instance of LinkedModeUI. */
	protected class LinkedModeUI playedBy LinkedModeUI 
	{
		/** After an enter call has been issued and before leave is called,
		 *  make this instance available.
		 */ 
		void register () {
			LinkedModeAdaptor.this.ui = this;
		}
		register <- after enter;
		
		/** Remove instance, no longer in linked mode. */
		void reset() {
			LinkedModeAdaptor.this.ui = null;
		}
		reset <- after leave;

		@SuppressWarnings("decapsulation")
		protected
		void leave(int flags) -> void leave(int flags);
	}
	
	/**
	 * Request to leave the linked mode.
	 * Checks availability of a LinkedModeUI instance.
	 * 
	 * @return true if successful
	 */
	public boolean leaveLinkedMode() {
		if (this.ui == null)
			return false;
		this.ui.leave(ILinkedModeListener.EXIT_ALL);
		return true;
	}
}
