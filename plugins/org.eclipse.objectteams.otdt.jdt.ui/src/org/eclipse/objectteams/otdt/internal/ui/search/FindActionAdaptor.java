/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: FindActionAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.search;

import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.internal.core.AbstractCalloutMapping;

import base org.eclipse.jdt.ui.actions.FindAction;
import base org.eclipse.jdt.ui.actions.FindDeclarationsAction;
import base org.eclipse.jdt.ui.actions.FindImplementorsAction;
import base org.eclipse.jdt.ui.actions.FindReferencesAction;

/**
 * Find actions have hard-coded lists of classes for which they are applicable.
 * Insert method bindings as appropriate to these lists.
 * 
 * @author stephan
 */
@SuppressWarnings({"restriction", "unchecked"}) // using raw type 'Class' 
public team class FindActionAdaptor 
{
	protected class FindAction playedBy FindAction 
	{
		Class[] addClass(Class[] result, Class class1) {
			int len = result.length;
			System.arraycopy(result, 0, result = new Class[len+1], 0, len);
			result[len] = class1;
			return result;
		}
	}
	
	protected class FindDeclarationsAction extends FindAction playedBy FindDeclarationsAction {
		callin Class[] getValidTypes () {
			return addClass(base.getValidTypes(), AbstractCalloutMapping.class);
		}
		// TODO(SH): pulling-up this binding to FindAction fails due to bcel-Repository
		@SuppressWarnings("decapsulation")
		getValidTypes <- replace getValidTypes;
	}
	protected class FindImplementorsAction extends FindAction playedBy FindImplementorsAction {
		callin Class[] getValidTypes () {
			return addClass(base.getValidTypes(), AbstractCalloutMapping.class);
		}
		@SuppressWarnings("decapsulation")
		getValidTypes <- replace getValidTypes;
	}
	protected class FindReferencesAction extends FindAction playedBy FindReferencesAction {
		callin Class[] getValidTypes () {
			return addClass(base.getValidTypes(), IMethodMapping.class);
		}
		@SuppressWarnings("decapsulation")
		getValidTypes <- replace getValidTypes;
	}
}
