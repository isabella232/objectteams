/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Stephan Herrmann.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AspectBindingsTreeNode.java 23470 2010-02-05 19:13:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.pde.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otequinox.Constants;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.plugin.PluginElement;

/** 
 * Nodes for representing aspect bindings in the package explorer.
 * @author stephan
 * @since 1.3.2
 */
@SuppressWarnings("restriction")
public abstract class AspectBindingsTreeNode {
	IJavaProject javaProject;
}

// ========== follows: concrete subclasses ==========

	class AspectBindingsRootNode extends AspectBindingsTreeNode
	{
		static final String ASPECT_BINDINGS_NAME = "OT/Equinox Aspect Bindings";
		protected IPluginModelBase pluginModel;
		public AspectBindingsRootNode(IJavaProject javaProject, IPluginModelBase pluginModel) {
			this.javaProject = javaProject;
			this.pluginModel = pluginModel;
		}
		@Override
		public String toString() {
			return "~"+ASPECT_BINDINGS_NAME; // prepend "~" to make sorting a bit more deterministic, should we encounter CUDs at toplevel
		}
	}
	
	/** Represents the aspect bindings for one base plugin. */
	@SuppressWarnings("restriction")
	class BasePluginNode extends AspectBindingsTreeNode
	{
		protected String basePlugin;
		boolean hasForcedExports = false; 
		PluginElement element;
		Object[] teams = null;
		
		public BasePluginNode(IJavaProject javaProject, PluginElement element) {
			this.javaProject = javaProject;
			this.element = element;
			for (IPluginObject child : element.getChildren()) {
				if (child instanceof PluginElement) {
					if (Constants.BASE_PLUGIN.equals(child.getName())) {
						this.basePlugin = ((PluginElement)child).getAttribute("id").getValue();
						for (IPluginObject subChild : ((PluginElement)child).getChildren())
							if (Constants.FORCED_EXPORTS_ELEMENT.equals(((PluginElement)subChild).getName()))
								this.hasForcedExports = true;
						break;
					}
				}
			}
		}
		/** Add the teams of other to this node. */
		protected void merge(BasePluginNode other) {
			Object[] myTeams = getTeams();
			Object[] otherTeams = other.getTeams();
			int l1=myTeams.length, l2=otherTeams.length;
			teams = new Object[l1+l2];
			System.arraycopy(myTeams, 0, this.teams, 0, l1);
			System.arraycopy(otherTeams, 0, this.teams, l1, l2);
		}
		/** Get all teams adapting the base plugin represented by this node. */
		protected Object[] getTeams() {
			if (this.teams != null)
				return this.teams;
			List<Object> teams = new ArrayList<Object>();
			for (IPluginObject child : element.getChildren())
				if (child instanceof PluginElement)
					if (Constants.TEAM.equals(child.getName()))
						teams.add(new TeamNode(this.javaProject, 
											   ((PluginElement)child).getAttribute("class").getValue()));
			return this.teams = teams.toArray();
		}
		protected Object getPluginXml() {
			return this.javaProject.getProject().findMember("plugin.xml");
		}
	}
	/**
	 * Handle to a team referenced in an aspect binding, 
	 * supports on demand resolving to an IType.
	 */
	class TeamNode extends AspectBindingsTreeNode
	{
		protected String teamName;
	
		protected TeamNode(IJavaProject javaProject, String teamName) {
			this.javaProject = javaProject;
			this.teamName = teamName;
		}

		protected IType getTeamType() {
			try {
				return this.javaProject.findType(this.teamName);
			} catch (JavaModelException e) {
				OTPDEUIPlugin.getDefault().getLog().log(
					OTPDEUIPlugin.createErrorStatus("OpenAction: Cannot resolve team type '"+this.teamName+"'", e));
				return null;
			}
		}
	}

