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
 *		Stephan Herrmann - Initial API and implementation
 * 		IBM Corporation - Some individual method and fragments
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.compare;

import java.io.UnsupportedEncodingException;

import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IEditableContentExtension;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ISharedDocumentAdapter;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.SharedDocumentAdapter;
import org.eclipse.compare.contentmergeviewer.IDocumentRange;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.SharedDocumentAdapterWrapper;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IDocument;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.texteditor.IDocumentProvider;


/**
 * Represents the role method of a bound-methods-comparison.
 * 
 * This class is inspired by class org.eclipse.jdt.internal.ui.compare.JavaNode.
 * 
 * @author stephan
 * @since 1.4.0
 */
class RoleMethodNode extends DocumentRangeNode implements ITypedElement {

	public static final int CU= 0;
	public static final int METHOD= 11;

	private static final String COMPILATION_UNIT = "compilationUnit"; //$NON-NLS-1$

	private static final char METHOD_ID= '~';
	private static final char COMPILATIONUNIT= '{';
	
	static String buildID(int type, String name) {
		StringBuffer sb= new StringBuffer();
		switch (type) {
		case RoleMethodNode.CU:
			sb.append(COMPILATIONUNIT);
			break;
		case RoleMethodNode.METHOD:
			sb.append(METHOD_ID);
			sb.append(name);
			break;
		default:
			Assert.isTrue(false);
			break;
		}
		return sb.toString();
	}
	
	/**
	 * The root node of the compilation/document unit containing the role method.
	 * This node is responsible for saving the comparison.
	 */
	static class RootJavaNode extends RoleMethodNode implements IDisposable {

		private final Object fInput;
		private final ISharedDocumentAdapter fAdapter;

		RootJavaNode(IDocument document, Object input, ISharedDocumentAdapter adapter) {
			super(document);
			fInput= input;
			fAdapter= adapter;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.compare.structuremergeviewer.DocumentRangeNode#isEditable()
		 */
		public boolean isEditable() {
			return true;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.compare.structuremergeviewer.DocumentRangeNode#nodeChanged(org.eclipse.compare.structuremergeviewer.DocumentRangeNode)
		 */
		protected void nodeChanged(DocumentRangeNode node) {
			save(this, this.fInput);
		}
		
		void save(IStructureComparator node, Object input) {
			if (node instanceof IDocumentRange && input instanceof IEditableContent) {
				IDocument document= ((IDocumentRange)node).getDocument();
				// First check to see if we have a shared document
				final ISharedDocumentAdapter sda = SharedDocumentAdapterWrapper.getAdapter(input);
				if (sda != null) {
					IEditorInput key = sda.getDocumentKey(input);
					if (key != null) {
						IDocumentProvider provider = SharedDocumentAdapter.getDocumentProvider(key);
						if (provider != null) {
							IDocument providerDoc = provider.getDocument(key);
							// We have to make sure that the document we are saving is the same as the shared document
							if (providerDoc != null && providerDoc == document) {
								if (save(provider, document, input, sda, key))
									return;
							}
						}
					}
				}
				IEditableContent bca= (IEditableContent) input;
				String contents= document.get();
				String encoding= null;
				if (input instanceof IEncodedStreamContentAccessor) {
					try {
						encoding= ((IEncodedStreamContentAccessor)input).getCharset();
					} catch (CoreException e1) {
						// ignore
					}
				}
				if (encoding == null)
					encoding= ResourcesPlugin.getEncoding();
				byte[] bytes;
				try {
					bytes= contents.getBytes(encoding);
				} catch (UnsupportedEncodingException e) {
					bytes= contents.getBytes();
				}
				bca.setContent(bytes);
			}
		}
		
		boolean save(final IDocumentProvider provider, final IDocument document,
				final Object input, final ISharedDocumentAdapter sda, final IEditorInput key) {
			try {
				sda.flushDocument(provider, key, document, false);
				return true;
			} catch (CoreException e) {
				OTDTUIPlugin.log(e);
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.services.IDisposable#dispose()
		 */
		public void dispose() {
			if (fAdapter != null) {
				fAdapter.disconnect(fInput);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.compare.structuremergeviewer.DocumentRangeNode#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class adapter) {
			if (adapter == ISharedDocumentAdapter.class) {
				return fAdapter;
			}
			return super.getAdapter(adapter);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.compare.structuremergeviewer.DocumentRangeNode#isReadOnly()
		 */
		public boolean isReadOnly() {
			if (fInput instanceof IEditableContentExtension) {
				IEditableContentExtension ext = (IEditableContentExtension) fInput;
				return ext.isReadOnly();
			}
			return super.isReadOnly();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.compare.structuremergeviewer.DocumentRangeNode#validateEdit(org.eclipse.swt.widgets.Shell)
		 */
		public IStatus validateEdit(Shell shell) {
			if (fInput instanceof IEditableContentExtension) {
				IEditableContentExtension ext = (IEditableContentExtension) fInput;
				return ext.validateEdit(shell);
			}
			return super.validateEdit(shell);
		}
	}
	
	/**
	 * Creates a RoleMethodNode under the given parent.
	 * @param parent the parent node
	 * @param type the Java elements type. Legal values are from the range CU to METHOD of this class.
	 * @param name the name of the Java element
	 * @param start the starting position of the java element in the underlying document
	 * @param length the number of characters of the java element in the underlying document
	 */
	public RoleMethodNode(RoleMethodNode parent, int type, String name, int start, int length) {
		super(parent, type, buildID(type, name), parent.getDocument(), start, length);
		parent.addChild(this);
	}

	/**
	 * Creates a RoleMethodNode for a CU. It represents the root of a
	 * RoleMethodNode tree, so its parent is null.
	 * @param document the document which contains the Java element
	 */
	public RoleMethodNode(IDocument document) {
		super(CU, buildID(CU, "root"), document, 0, document.getLength()); //$NON-NLS-1$
	}

	/**
	 * Returns a name which is presented in the UI.
	 * @see ITypedElement#getName()
	 */
	public String getName() {

		switch (getTypeCode()) {
		case CU:
			return COMPILATION_UNIT;
		}
		return getId().substring(1);	// we strip away the type character
	}

	/*
	 * @see ITypedElement#getType()
	 */
	public String getType() {
		return "java2"; //$NON-NLS-1$
	}

	/**
	 * Returns a shared image for this Java element.
	 *
	 * see ITypedInput.getImage
	 */
	public Image getImage() {
		return null; // never displayed in the UI
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getType() + ": " + getName() //$NON-NLS-1$
				+ "[" + getRange().offset + "+" + getRange().length + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}

