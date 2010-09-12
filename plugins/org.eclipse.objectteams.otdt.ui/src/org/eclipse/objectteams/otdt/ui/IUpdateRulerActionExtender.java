/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann.
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
 * 		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;

/** 
 * Interface for extensions to the extension point <code>org.eclipse.objectteams.otdt.ui.updateRulerActionExtenders</code>. 
 */
public interface IUpdateRulerActionExtender {
	/**
	 * This method is invoked when the underlying update ruler action is invoked on an editor that matches the current extension. 
	 */
	public void menuAboutToShow(IMenuManager contextMenu, IDocument document, IEditorPart editor, int line);
}
