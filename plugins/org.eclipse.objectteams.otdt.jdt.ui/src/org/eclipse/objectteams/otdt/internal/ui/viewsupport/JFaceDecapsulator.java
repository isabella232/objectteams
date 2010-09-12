/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JFaceDecapsulator.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.viewsupport;

import org.eclipse.swt.graphics.ImageData;

import base org.eclipse.jface.resource.CompositeImageDescriptor;

/**
 * The sole purpose of this aspect is to decapsulate one method 
 * (drawImage) of plug-in org.eclpise.jface.
 * This method is needed by sub-team ViewAdaptor, 
 * which adapts plug-in org.eclipse.jdt.ui.
 * 
 * Note, that this team is never activated (yet it has to be declared
 * in plugin.xml to announce our plug-in dependencies).
 * 
 * @author stephan
 */
public team class JFaceDecapsulator
{
	protected class CompositeImageDescriptor playedBy CompositeImageDescriptor
	{		
		// must be in this team (adapting the jface plug-in) to ensure decapsulation is woven in.
		@SuppressWarnings("decapsulation")
		void drawImage (ImageData data, int x, int y) -> void drawImage (ImageData data, int x, int y);
	}
}
