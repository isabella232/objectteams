/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009, Stephan Herrmann and others.
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
 * 		Stephan Herrmann - Initial API and implementation
 * 		IBM Corporation - Individual methods and code fragments.
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.compare;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.internal.ui.StorageTypedElement;
import org.eclipse.ui.IEditorInput;

/**
 * Represents the base method in a compare-bound-methods comparison.
 * The base method may be binary, this part of the comparison will never be editable.
 * 
 * @author stephan
 * @since 1.4.0
 */
public class BaseMethodCompareElement extends StorageTypedElement 
{
	IMethod method = null;

	public BaseMethodCompareElement(IMethod method, String localEncoding) {
		super(localEncoding);
		this.method = method;
	}
	public Image getImage() {
		return null; // never displayed in the UI
	}
	public String getName() {
		return this.method.getElementName();
	}
	public Object getTypeName() {
		return this.method.getDeclaringType().getElementName();
	}
	public String getType() {
		// cf. RoleMethodNode#getType()
		return "java2"; //$NON-NLS-1$
	}
	public IResource getResource() {
		return this.method.getResource();
	}

	@Override
	protected IStorage fetchContents(IProgressMonitor monitor) throws CoreException {
		return new IStorage() {
			public InputStream getContents() throws CoreException {
				String contents = getExtendedSource(BaseMethodCompareElement.this.method);
				byte[] bytes;
				try {
					bytes= contents.getBytes(getCharset());
				} catch (UnsupportedEncodingException e) {
					bytes= contents.getBytes();
				}
				return new ByteArrayInputStream(bytes);
			}
			public IPath getFullPath() {
				return null;
			}
			public String getName() {
				return method.getElementName();
			}
			public boolean isReadOnly() {
				return true;
			}
			public Object getAdapter(Class adapter) {
				return null;
			}
		};
	}
	
	private String getExtendedSource(IMethod method) throws JavaModelException 
	{
		// get parent
		IJavaElement parent= method.getParent();
		if (parent instanceof ISourceReference) {
			ISourceReference sr= (ISourceReference) parent;
			String parentContent= sr.getSource();
			if (parentContent != null) {
				ISourceRange parentRange= sr.getSourceRange();
				ISourceRange childRange= method.getSourceRange();

				int start= childRange.getOffset() - parentRange.getOffset();
				int end= start + childRange.getLength();

				// search backwards for beginning of line
				while (start > 0) {
					char c= parentContent.charAt(start-1);
					if (c == '\n' || c == '\r')
						break;
					start--;
				}

				return parentContent.substring(start, end);
			}
		}

		return method.getSource();
	}

	@Override
	protected IEditorInput getDocumentKey(Object element) {
		return null; // base side is not editable, hence no need to make it sharable.
	}
	
}