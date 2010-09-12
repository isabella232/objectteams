/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008, 2010 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ResourceProjectAdaptor.java 23451 2010-02-04 20:33:32Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.compiler.adaptor;

import org.objectteams.LiftingVetoException;
import org.objectteams.Team;

import base org.eclipse.core.internal.resources.Project;

/**
 * Simple decoration of class Project from org.eclipse.core.resources.
 *  
 * @author stephan
 * @since 1.1.8
 */
@SuppressWarnings("restriction")
public team class ResourceProjectAdaptor 
{
	private static ResourceProjectAdaptor instance;
	public ResourceProjectAdaptor() {
		instance= this;
	}
	public static ResourceProjectAdaptor getDefault() { return instance; }
	
	/** Associate an AspectBindingReader and a BaseImportChecker to each OT Plugin project. */
	protected class OTEquinoxProject playedBy Project
	{
		protected AspectBindingReader aspectBindingReader;
		protected BaseImportChecker checker;

		/** 
		 * Lifting constructor which refuses lifting for non OT-plugin projects,
		 * or if the project is not yet ready for reading plugin.xml. 
		 */
		public OTEquinoxProject(Project baseProject) {
			try {
				if (!ProjectUtil.isOTPluginProject(baseProject))
					throw new org.objectteams.LiftingVetoException();
				this.aspectBindingReader = new AspectBindingReader(baseProject); // may also throw LVE.
				this.checker= new BaseImportChecker(this.aspectBindingReader);
			} catch (LiftingVetoException lve) {
				// no success: unregister this useless role
				ResourceProjectAdaptor.this.unregisterRole(this, OTEquinoxProject.class);
				throw lve; // will be caught in several clients within AdaptorActivator
			}
		}
		/** ask for changes to determine if classpath has to be recomputed: */
		protected boolean hasAspectDataChanged () {
			return this.aspectBindingReader != null
				&& this.aspectBindingReader.fetchHasChanges();
		}
	}

	// ======== API: =========
	
	public Team getChecker(Project as OTEquinoxProject project) 
			throws LiftingVetoException 
	{
		return project.checker;
	}
	public AspectBindingReader getAspectBindingReader(Project as OTEquinoxProject project) 
			throws LiftingVetoException 
	{
		return project.aspectBindingReader;
	}
	public boolean hasAspectDataChanged(Project as OTEquinoxProject project)  
			throws LiftingVetoException 
	{
		return project.hasAspectDataChanged();
	}

}
