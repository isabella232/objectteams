/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Germany and Technical University Berlin, Germany.
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
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otequinox.internal;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.objectteams.otequinox.ActivationKind;

/** 
 * A simple record representing the information read from an extension to org.eclipse.objectteams.otequinox.aspectBindings.
 * @author stephan
 * @since 1.3.0 (was a nested class before that) 
 */
public class AspectBinding {
	public String aspectPlugin;
	public String basePlugin;
	public IConfigurationElement[] forcedExports;
	public ActivationKind[] activations = null; 
	public String[]         teamClasses;
	public List<String>[]   subTeamClasses;
	public boolean          activated= false;
	
	public AspectBinding(String aspectId, String baseId, IConfigurationElement[] forcedExportsConfs) {
		this.aspectPlugin= aspectId;
		this.basePlugin= baseId;
		this.forcedExports= forcedExportsConfs;
	}
	
	@SuppressWarnings("unchecked")
	public void initTeams(int count) {
		this.teamClasses    = new String[count];
		this.subTeamClasses = new List[count]; // new List<String>[count] is illegal!
		this.activations    = new ActivationKind[count];
	}
	
	public void setActivation(int i, String specifier) {
		if (specifier == null)
			this.activations[i] = ActivationKind.NONE;
		else
			this.activations[i] = ActivationKind.valueOf(specifier);
	}
	public String toString() {
		String result = "\tbase plugin "+basePlugin+"\n\tadapted by aspect pluging "+aspectPlugin;
		for (String teamClass : teamClasses) {
			result += "\n\t\t + team "+teamClass;
		}
		return result;
	}
}