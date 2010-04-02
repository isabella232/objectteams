/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTRuntimeVariableInitializer.java 23419 2010-02-03 20:43:26Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.ext;

import org.eclipse.jdt.core.ClasspathVariableInitializer;

/**
 * FIXME(SH): try if this VARIABLE can be removed altogether.
 * @author gis
 * @version $Id: OTRuntimeVariableInitializer.java 23419 2010-02-03 20:43:26Z stephan $
 */
public class OTRuntimeVariableInitializer extends ClasspathVariableInitializer
{
	public void initialize(String variable)
	{
		if (OTDTPlugin.OTRUNTIME_INSTALLDIR.equals(variable))
		{
			OTVariableInitializer.setPluginInstallationPathVariable(OTDTPlugin.getDefault(), variable);
		}
	}
}
