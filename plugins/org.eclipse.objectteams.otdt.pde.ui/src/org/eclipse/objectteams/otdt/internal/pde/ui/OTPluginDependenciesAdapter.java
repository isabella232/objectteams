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
 * $Id: OTPluginDependenciesAdapter.java 23470 2010-02-05 19:13:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.pde.ui;

import org.eclipse.objectteams.otequinox.Constants;
import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.ui.templates.PluginReference;

import base org.eclipse.pde.internal.ui.wizards.plugin.PluginClassCodeGenerator;

/**
 * Note that this team must be mentioned in plugin.xml (aspectBinding),
 * although it is not instantiated/activated globally, but only
 * temporarily during OTNewPluginProjectWizard.performFinish().
 * 
 * @author gis
 */
@SuppressWarnings("restriction")
public team class OTPluginDependenciesAdapter
{
	public class PluginClassCodeGeneratorAdapter playedBy PluginClassCodeGenerator
	{
		private final IPluginReference TRANSFORMER = new PluginReference(Constants.TRANSFORMER_PLUGIN_ID, null, 0);

		/**
		 * Returns Object Teams specific plugin dependencies additionally to the default
		 * plugin dependencies.
		 */
		callin IPluginReference[] getDependencies()
		{
			IPluginReference[] deps = base.getDependencies();
			for (IPluginReference dependency : deps) {
				if (dependency.equals(TRANSFORMER))
					return deps; // already included				
			}
			IPluginReference[] extraDeps = new IPluginReference[] { TRANSFORMER };
			return mergeArrays(deps, extraDeps);
		}

		IPluginReference[] mergeArrays(IPluginReference[] deps, IPluginReference[] extraDeps)
		{
			IPluginReference[] newDeps = new IPluginReference[deps.length + extraDeps.length];
			System.arraycopy(deps, 0, newDeps, 0, deps.length);
			System.arraycopy(extraDeps, 0, newDeps, deps.length, extraDeps.length);
			
			return newDeps;
		}
		
		IPluginReference[] getDependencies() <- replace IPluginReference[] getDependencies();
	}
}
