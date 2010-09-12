/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: RoleCreator.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.wizards.typecreation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.objectteams.otdt.ui.OTDTUIPluginConstants;


/**
 * A RoleCreator is responsible for creating a new role class.
 * 
 * @author kaschja
 * @version $Id: RoleCreator.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class RoleCreator extends TypeCreator
{
    
	/**
	 * @return "java.lang.Object"
	 */
	protected String createDefaultSupertypeName()
	{
		return "java.lang.Object"; //$NON-NLS-1$
	}


	protected void writeInheritanceRelations(ImportsManager imports, StringBuffer buf) throws CoreException
	{
        super.writeInheritanceRelations(imports, buf);
		writeBaseClass(buf, imports.fImportsRewrite);
	}

	private void writeBaseClass(StringBuffer buf, ImportRewrite imports)
	{
	    if ( !(getTypeInfo() instanceof RoleTypeInfo) )
			return;
	    
	    RoleTypeInfo typeInfo = (RoleTypeInfo) getTypeInfo();
	    String baseName = typeInfo.getBaseTypeName();
		
		if (baseName != null && baseName.trim().length() > 0 )
		{
			buf.append(" playedBy "); //$NON-NLS-1$
			buf.append(imports.addImportBase(baseName));
		}
	}


	protected void validateTypeCreation()
		throws CoreException
	{		
		if (getTypeInfo().getEnclosingTypeName().trim().length() == 0)
		{
			throw new CoreException(new Status(IStatus.ERROR,
			                                   OTDTUIPluginConstants.UIPLUGIN_ID, 
											   IStatus.OK, 
                                               "The role class " + getTypeInfo().getTypeName() + " must have an enclosing team.", //$NON-NLS-1$ //$NON-NLS-2$
											   null));
		}
	}
}
