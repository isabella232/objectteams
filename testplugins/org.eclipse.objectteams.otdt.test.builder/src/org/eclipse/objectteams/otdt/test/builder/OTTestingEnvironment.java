/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
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
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.test.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import base org.eclipse.jdt.core.tests.builder.TestingEnvironment;

/** Purpose: add OTJ_NATURE to test projects (method not normally overridable). */
public team class OTTestingEnvironment {
	
	protected class TestingEnvironment playedBy TestingEnvironment {

		@SuppressWarnings("decapsulation")
		void handleCoreException(CoreException e) -> void handleCoreException(CoreException e);

		IProject getProject(String projectName) -> IProject getProject(String projectName);

		@SuppressWarnings("basecall")
		callin void addBuilderSpecs(String projectName) {
			try {
				IProject project = getProject(projectName);
				IProjectDescription description = project.getDescription();
				description.setNatureIds(new String[] { JavaCore.NATURE_ID, JavaCore.OTJ_NATURE_ID });
				project.setDescription(description, null);
			} catch (CoreException e) {
				handleCoreException(e);
			}
		}

		@SuppressWarnings("decapsulation")
		addBuilderSpecs <- replace addBuilderSpecs;
		
	}
}
