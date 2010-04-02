/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTJBuilder.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.builder;

import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.core.builder.IncrementalImageBuilder;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;

/**
 * Extend the JavaBuilder for handling role files.
 * 
 * @author stephan
 * @version $Id: OTJBuilder.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class OTJBuilder extends JavaBuilder {
	
	// place this builder into the name space of the plug-in org.eclipse.objectteams.otdt:
	public static final String BUILDER_ID = "org.eclipse.objectteams.otdt.builder.OTJBuilder"; //$NON-NLS-1$

	public static String getName()
	{
		return BUILDER_ID;
	}

	/** 
	 * Create and initialize a matching IncrementalImageBuilder.
	 */
	protected IncrementalImageBuilder makeImageBuilder(SimpleLookupTable deltas) {
		return new OTJIncrementalImageBuilder(this);
	}
	
}
