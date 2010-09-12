/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JavaOutlinePageAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.javaeditor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.swt.widgets.Item;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.ui.Util;

import base org.eclipse.jdt.internal.ui.javaeditor.JavaOutlinePage.JavaOutlineViewer;
import base org.eclipse.jdt.internal.ui.javaeditor.JavaOutlinePage.ChildrenProvider;

/**
 * The only purpose: filter generated elements from the outline page.
 *  
 * @author stephan
 */
@SuppressWarnings({ "restriction", "decapsulation" })
public team class JavaOutlinePageAdaptor 
{
	protected class ContentProviderAdaptor playedBy ChildrenProvider
	{
		matches <- replace matches;
		callin boolean matches(IJavaElement element) {
			if (base.matches(element))
				return true;
			return Util.isGenerated(element);
		}
	}
	protected class Viewer playedBy JavaOutlineViewer 
	{
		void unwrapOTType(Object element) <- replace void associate(Object element, Item item)
			base when (element instanceof IOTType); 

		callin void unwrapOTType(Object element) {
			base.unwrapOTType(((IOTType)element).getCorrespondingJavaElement());
		}		
	}
}
