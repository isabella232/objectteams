/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JavaElementMarkable.java 23435 2010-02-04 00:14:38Z stephan $
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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Implement markable protocol for IClassFile elements.
 * 
 * @author stephan
 * @since 1.2.5
 */
public class JavaElementMarkable extends AbstractMarkable {
	IClassFile fJavaElement;

	/**
	 * @param javaElement should actually be an IClassFile (otherwise use ResourceMarkable on the IFile (source file))
	 */
	public JavaElementMarkable(IClassFile javaElement) {
		this.fJavaElement = javaElement;
	}

	public String getName() {
		return this.fJavaElement.getElementName();
	}
	
	public Set<IType> getAllTypes(IJavaProject[] projects, IProgressMonitor monitor) throws JavaModelException {
		Set<IType> result = new HashSet<IType>(13);
		
		IType type = this.fJavaElement.getType();

		Set<IType> members = new HashSet<IType>(5);
		Set<IType> supers = new HashSet<IType>(5);
		members.add(type);
		addSuperAndMemberTypes(members, supers, type, this.fJavaElement.getJavaProject(), projects, monitor);
		result.addAll(members);
		result.addAll(supers);
		monitor.worked(5);
		
		result.addAll(getSubTypes(members, new MySubProgressMonitor(monitor, 5)));

		monitor.done();
		return result;
	}

	public void removeCallinMarkers() throws CoreException {
		CallinMarkerRemover.removeCallinMarkers(this.fJavaElement);		
	}

	public IClassFile getJavaElement() {
		return this.fJavaElement;
	}
	
	public IResource getResource() {
		return this.fJavaElement.getResource();
	}
	
	boolean containsElement(IJavaElement element) {
		IJavaElement current = element;
		while (current != null) {
			if (current == this.fJavaElement)
				return true;
			current = current.getParent();
		}
		return false;
	}

	public boolean exists() {
		return this.fJavaElement.exists();
	}
	
	IJavaProject[] getProjects() throws JavaModelException {
		// FIXME(SH): might want to lookup the project from a library??
		return getProjects(this.fJavaElement.getCorrespondingResource());
	}

	public IMarker createMarker(String id) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IMarker marker = root.createMarker(id);
		((JavaCore)JavaCore.getPlugin()).configureJavaElementMarker(marker, this.fJavaElement);
		return marker;
	}

}
