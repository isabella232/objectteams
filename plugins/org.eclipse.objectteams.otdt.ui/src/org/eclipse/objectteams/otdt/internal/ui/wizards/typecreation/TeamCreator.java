/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2008 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TeamCreator.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;


/**
 * A TeamCreator is responsible for creating a new team class.
 * 
 * @author kaschja
 * @version $Id: TeamCreator.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class TeamCreator extends TypeCreator
{

	/**
	 * @return "org.objectteams.Team"
	 */
	protected String createDefaultSupertypeName()
	{
		return String.valueOf(IOTConstants.STR_ORG_OBJECTTEAMS_TEAM);
	}

	protected ITypeHierarchy createSuperTypeHierarchy(IType type,
            IProgressMonitor monitor) throws JavaModelException
    {
		IOTType otType = OTModelManager.getOTElement(type);
		if (otType != null)
		    return otType.newSuperOTTypeHierarchy(monitor);
		
		return super.createSuperTypeHierarchy(type, monitor);
    }
}
