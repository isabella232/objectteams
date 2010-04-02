/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTCoreExtMessages.java 23427 2010-02-03 22:23:59Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.ext;

import org.eclipse.osgi.util.NLS;

public class OTCoreExtMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.objectteams.otdt.core.ext.OTCoreExtMessages"; //$NON-NLS-1$

	public static String OTREContainer__Description;

	public static String OTREContainer_otre_not_found;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, OTCoreExtMessages.class);
	}

	private OTCoreExtMessages() {
	}
}
