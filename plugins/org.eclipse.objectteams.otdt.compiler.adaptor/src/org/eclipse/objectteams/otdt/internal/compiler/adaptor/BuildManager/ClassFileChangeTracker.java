/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ClassFileChangeTracker.java 23451 2010-02-04 20:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
team package org.eclipse.objectteams.otdt.internal.compiler.adaptor.BuildManager;

import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;


/**
 * This class tracks whenever a classfile has non-structural changes.
 * In that case a re-compile is needed iff the class is a role of
 * which copies exist in subteams within the workspace/project.
 *  
 * @author stephan
 */
@SuppressWarnings("restriction")
public class ClassFileChangeTracker playedBy ClassFileReader 
{	
	void nonStructuralChange(String className) <- after boolean hasStructuralChanges(byte[] newBytes)
			when (!result)
			with {className <- new String(base.getName())}
			
	/** No structural changes where detected, yet bytes differ.*/
	// static for optimization (avoid lifting).
	protected static void nonStructuralChange(String className) {
		className = className.replace('/', '.');
		if (DEBUG >= 2)
			System.out.println("Non-structural change for "+className); //$NON-NLS-1$
		int dollarPos = className.lastIndexOf('$');
		if (dollarPos == -1)
			return; // not a role
		String roleName = canonicalName(className.substring(dollarPos+1)); //excluding '$'	
		Set<String> teamSourceFileNames = BuildManager.this.roleToSubTeams.get(className);
		if (teamSourceFileNames != null)
			for (String teamSourceFileName : teamSourceFileNames) { 
				if (DEBUG > 0)
					System.out.println("need to recompile "+teamSourceFileName);  //$NON-NLS-1$
				BuildManager.this.teamsToRecompile.add(teamSourceFileName);
				String teamTypeName = teamSourceFileName.substring(0, teamSourceFileName.length()-5); // .java
				// mark both parts (class/ifc) as stale:
				BuildManager.this.staleRoles.add(teamTypeName+'$'+roleName);
				BuildManager.this.staleRoles.add(teamTypeName+'$'+IOTConstants.OT_DELIM+roleName);
			}		
	}
}
