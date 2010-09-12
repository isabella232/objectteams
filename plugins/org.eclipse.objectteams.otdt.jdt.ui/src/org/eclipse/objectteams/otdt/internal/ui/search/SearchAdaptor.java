/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SearchAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.search;

import org.eclipse.objectteams.otdt.core.IOTType;

import base org.eclipse.jdt.internal.ui.search.LevelTreeContentProvider.FastJavaElementProvider;

/**
 * This adaptor ensures that OT-Types are not displayed duplicately in the search result page,
 * which happened due to the duality of, e.g., SourceType and RoleType.
 * See http://trac.objectteams.org/ot/ticket/44
 * @author stephan
 */
@SuppressWarnings({ "restriction", "decapsulation" })
public team class SearchAdaptor {
	protected class ContentProviderAdaptor playedBy FastJavaElementProvider
	{

		Object getParent(Object element) <- replace Object getParent(Object element);

		/** Normalize IOTTypes by using the corresponding java element. */
		callin Object getParent(Object element) 
		{
			Object result= base.getParent(element);
			if (result instanceof IOTType)
				result= ((IOTType)result).getCorrespondingJavaElement();
			return result;
		}
		
	}
}
