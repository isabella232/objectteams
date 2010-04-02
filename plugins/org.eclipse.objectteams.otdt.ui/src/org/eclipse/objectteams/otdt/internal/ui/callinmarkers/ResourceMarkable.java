/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ResourceMarkable.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.callinmarkers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Implement the markable protocol for resources.
 * 
 * @author stephan
 * @since 1.2.5
 */
public class ResourceMarkable extends AbstractMarkable {
	IResource  fResource;

	public ResourceMarkable(IResource resource) {
		this.fResource = resource;
	}

	public String getName() {
		return this.fResource.getName();
	}

	public Set<IType> getAllTypes(IJavaProject[] projects, IProgressMonitor monitor) throws CoreException {
		Set<IType> result = new HashSet<IType>(13);
		IJavaElement element = JavaCore.create(this.fResource);
		if (element instanceof ICompilationUnit) 
		{			
			Set<IType> members = new HashSet<IType>(5);
			Set<IType> supers = new HashSet<IType>(5);
			for (IType type : ((ICompilationUnit) element).getTypes()) {
				members.add(type);	
				addSuperAndMemberTypes(members, supers, type, JavaCore.create(this.fResource.getProject()), projects, monitor);
			}
			result.addAll(members);
			result.addAll(supers);
			monitor.worked(5);
				
			result.addAll(getSubTypes(members, new MySubProgressMonitor(monitor, 5)));
		}
		return result;
	}
	IJavaProject[] getProjects() throws JavaModelException {
		return getProjects(this.fResource);
	}

	public void removeCallinMarkers() throws CoreException {
		CallinMarkerRemover.removeCallinMarkers(this.fResource);		
	}

	public IJavaElement getJavaElement() {
		return JavaCore.create(this.fResource);
	}
	
	public IResource getResource() {
		return this.fResource;
	}

	boolean containsElement(IJavaElement element) {
		return this.fResource.equals(element.getResource());
	}
	
	public boolean exists() {
		if (!this.fResource.exists()) return false;
		IJavaElement javaElement = getJavaElement();
		do {
			if (!javaElement.exists())
				return false;
			javaElement = javaElement.getParent();
		} while (javaElement.getElementType() != IJavaElement.PACKAGE_FRAGMENT_ROOT);
		return true;
	}
	
	public IMarker createMarker(String id) throws CoreException {
		return this.fResource.createMarker(id);		
	}
}
