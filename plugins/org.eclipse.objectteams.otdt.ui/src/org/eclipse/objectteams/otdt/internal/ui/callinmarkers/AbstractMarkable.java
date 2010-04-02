/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AbstractMarkable.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.callinmarkers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.objectteams.otdt.core.compiler.OTNameUtils;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;


/**
 * Abstraction for buffers that can hold markers in the ruler:
 * <ul>
 * <li>resources representing source files
 * <li>java elements representing binary class files
 * </ul>
 * 
 * @author stephan
 * @since 1.2.5
 */
public abstract class AbstractMarkable {

	final static String PDE_PLUGIN_NATURE_ID = "org.eclipse.pde.PluginNature"; //$NON-NLS-1$ // avoid importing org.eclipse.pde.core

	/** Name of this markable for use in the UI. */
	abstract String getName();

	/** Remove all callin markers from this markable. */
	abstract void removeCallinMarkers() throws CoreException;

	abstract IJavaElement getJavaElement();

	abstract IResource getResource();
	
	/** Create a real marker with the given ID. */
	abstract IMarker createMarker(String id) throws CoreException;
	
	/** 
	 * Get all types that contribute members to the current markable.
	 * Contribution happens through containment (members) and inheritance. 
	 */
	abstract Set<IType> getAllTypes(IJavaProject[] projects, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Get all projects that could be relevant for searching.
	 */
	abstract IJavaProject[] getProjects() throws JavaModelException;

	/**
	 * Is the given element contained in this markable?
	 */
	abstract boolean containsElement(IJavaElement element);
	
	/**
	 * Fetch the member and super types of type and add them to the given sets.
	 * Works recursively.
	 * 
	 * @param members  all members, direct and indirect, including inherited members.
	 * @param supers   all super types of this and its members.
	 * @param type     focus type
	 * @param currentProject the project of the current Markable, use this first when searching types.
	 * @param projects       where to search for super types which are given by their name
	 * @param monitor
	 * @throws JavaModelException
	 */
	void addSuperAndMemberTypes(Set<IType> members, Set<IType> supers, IType type, IJavaProject currentProject, IJavaProject[] projects, IProgressMonitor monitor) 
			throws JavaModelException 
	{
		if (!type.exists())
			return; // mh?
		
		IType superType = null;
		String superclassName = type.getSuperclassName();
		String packageName = null;
		String typeName = null;
		if (superclassName != null) {
			superclassName = superclassName.replace('$', '.');
			if (superclassName.indexOf('.') != -1) {
				// qualified, find the type directly:
				superType = currentProject.findType(superclassName);
			} else {
				// resolve it now:
				String[][] resolvedSuperName = type.resolveType(superclassName);
				if (resolvedSuperName != null && resolvedSuperName.length == 1) {
					packageName = resolvedSuperName[0][0];
					typeName    = resolvedSuperName[0][1];
					if (!(packageName.equals("java.lang") && typeName.equals("Object"))) //$NON-NLS-1$ //$NON-NLS-2$
					{
						superType = currentProject.findType(packageName, typeName, (IProgressMonitor)null);
						
						if (superType == null) 
							for (IJavaProject prj : projects)
								if ((superType = prj.findType(packageName, typeName, (IProgressMonitor)null)) != null)
									break;
					}
				}
			}
		}

		if (superType != null && !superType.isAnonymous()) {
			supers.add(superType);
			if (!members.contains(superType)) // avoid super-member-loop
				addSuperAndMemberTypes(members, supers, superType, currentProject, projects, monitor);					
		}

		for (IType member : type.getTypes()) {
			if (member.isInterface()) continue; // not currently bindable
			if (   Flags.isRole(member.getFlags())
				&& OTNameUtils.isTopConfined(member.getElementName())) continue; // confineds are not bound base
			members.add(member);
			if (!supers.contains(member)) // avoid super-member-loop
				addSuperAndMemberTypes(members, supers, member, currentProject, projects, monitor);
		}
	}
	/**
	 * Get all direct and indirect subtypes of all types in 'types'.
	 * @param types
	 * @param monitor
	 * @return
	 * @throws JavaModelException
	 */
	Set<IType> getSubTypes(Set<IType> types, IProgressMonitor monitor) throws JavaModelException {
		monitor.beginTask("base class hierarchy", types.size());

		IJavaSearchScope workspaceScope = SearchEngine.createWorkspaceScope();
		Set<IType> subTypes = new HashSet<IType>(13);
		for (IType type: types) {
			TypeHierarchy hier = new TypeHierarchy(type, null, workspaceScope, true);
			hier.refresh(monitor);
			for(IType subType : hier.getSubtypes(type))
				subTypes.add(subType);
			monitor.worked(1);
		}
		monitor.done();
		return subTypes;
	}

	/** Fetch projects from a resource or all workspace projects if resource === null */
	IJavaProject[] getProjects(IResource resource) {
		IJavaProject[] projects = null;
        if (resource != null) {
        	IProject project = resource.getProject();
        	projects = getProjectsToSearch(project);
        } else {
        	IWorkspace ws = ResourcesPlugin.getWorkspace();
        	ArrayList<IJavaProject> projectList = new ArrayList<IJavaProject>(); 
        	for (IProject prj : ws.getRoot().getProjects())
        		if (isOTProject(prj) && prj.isOpen()) // FIXME(SH): better project filtering
        			projectList.add(JavaCore.create(prj));
        	projects = projectList.toArray(new IJavaProject[projectList.size()]);
        }
		return projects;
	}

    private IJavaProject[] getProjectsToSearch(IProject baseProject)
    {
        Set<IJavaProject> result = new HashSet<IJavaProject>();
        calculateProjectsToSearch(baseProject, result);
        return result.toArray(new IJavaProject[result.size()]);
    }
        
    private boolean isOTProject(IProject project)
    {
        try
        {
            return project.hasNature(JavaCore.OTJ_NATURE_ID);
        }
        catch (CoreException ex)
        {
	        return false;
        }
    }
    
    private boolean isPluginProject(IProject project)
    {
        try
        {
			return project.hasNature(PDE_PLUGIN_NATURE_ID);
        }
        catch (CoreException ex)
        {
	        return false;
        }
    }
    
    private void calculateProjectsToSearch(IProject currentProject, Set<IJavaProject> allProjects)
    {
        if (isOTProject(currentProject))
        {
            allProjects.add(JavaCore.create(currentProject));
        }
        
//        if (isPluginProject(currentProject))
//        	return; // don't search indirect dependencies of plug-in projects because aspectBindings must be declared directly 
        
        IProject[] referencingProjects = currentProject.getReferencingProjects();
        
        for (int i = 0; i < referencingProjects.length; i++)
        {
            IProject project = referencingProjects[i];
            calculateProjectsToSearch(project, allProjects);
        }
    }

    /** Does this represent a real JavaElement (including existing ancestors)? */
	public abstract boolean exists();
}
