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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ISharedDocumentAdapter;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.compare.JavaStructureCreator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.objectteams.otdt.internal.ui.OTDTUIMessages;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.internal.ui.mapping.AbstractCompareInput;
import org.eclipse.team.internal.ui.mapping.CompareInputChangeNotifier;
import org.eclipse.team.internal.ui.synchronize.EditableSharedDocumentAdapter;
import org.eclipse.team.ui.synchronize.SaveableCompareEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;

/**
 * The editor input for comparing bound methods.
 * 
 * @author stephan
 * @since 1.4.0
 */
public class CompareBoundMethodsEditorInput extends SaveableCompareEditorInput {
	
	// editable LHS:
	private ITypedElement left;
	private IMethod roleMethod;
	// readonly RHS:
	private BaseMethodCompareElement right;
	
	CompareInputChangeNotifier notifier = new CompareInputChangeNotifier() {
		protected IResource[] getResources(ICompareInput input) {
			IResource resource = getResource();
			if (resource == null)
				return new IResource[0];
			return new IResource[] { resource };
		}
	};
	
	EditableSharedDocumentAdapter sharedDocumentAdapter;

	// hold on to a control that we want to hide:
	private Composite outline;
	
	class MyJavaStructureCreator extends JavaStructureCreator {
		/* (non-Javadoc)
		 * @see org.eclipse.compare.structuremergeviewer.StructureCreator#createStructureComparator(java.lang.Object, org.eclipse.jface.text.IDocument, org.eclipse.compare.ISharedDocumentAdapter, org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStructureComparator createStructureComparator(Object element,
																 IDocument document, 
																 ISharedDocumentAdapter sharedDocumentAdapter,
																 IProgressMonitor monitor) 
				throws CoreException 
		{
			if (document != null)
				return new RoleMethodNode.RootJavaNode(document, element, sharedDocumentAdapter);
			return null;
		}
	}
	
	/*
	 * Returns <code>true</code> if the other object is of type
	 * <code>CompareBoundMethodsEditorInput</code> and both of their
	 * corresponding left and right objects are identical. The content is not
	 * considered.
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof CompareBoundMethodsEditorInput) {
			CompareBoundMethodsEditorInput other = (CompareBoundMethodsEditorInput) obj;
			return other.left.equals(this.left)
					&& other.right.equals(this.right);
		}
		return false;
	}
	
	public class MyDiffNode extends AbstractCompareInput {
		public MyDiffNode(ITypedElement left, ITypedElement right) {
			super(Differencer.CHANGE, null, left, right);
		}
		public void fireChange() {
			super.fireChange();
		}
		protected CompareInputChangeNotifier getChangeNotifier() {
			return notifier;
		}
		public boolean needsUpdate() {
			// The base never changes
			return false;
		}
		public void update() {
			fireChange();
		}
	}
	
	/**
	 * Creates a new CompareBoundMethodsEditorInput.
	 * @param left 
	 * @param right 
	 * @param page 
	 * @throws JavaModelException 
	 */
	public CompareBoundMethodsEditorInput(IMethod roleMethod, IMethod baseMethod, IWorkbenchPage page) throws CoreException {
		super(new CompareConfiguration(), page);
		this.roleMethod = roleMethod;
		this.left = makeJavaNode();
		this.right = new BaseMethodCompareElement(baseMethod, getEncoding(roleMethod));
		setTitle(left.getName());
	}

	private ITypedElement makeJavaNode() throws CoreException 
	{
		// this structure creator is responsible for setup of a sharedDocumentAdapter while building the root node:
		MyJavaStructureCreator creator = new MyJavaStructureCreator();
		IStructureComparator rootNode = creator.createStructure(this, null);

		// create the detail node (role method):
		ISourceRange sourceRange = this.roleMethod.getSourceRange();
		return new RoleMethodNode((RoleMethodNode)rootNode, RoleMethodNode.METHOD, this.roleMethod.getElementName(), sourceRange.getOffset(), sourceRange.getLength());
	}

	private String getEncoding(IMethod method) {
		IResource resource = method.getResource();
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			try {
				return file.getCharset();
			} catch (CoreException e) {
				OTDTUIPlugin.log(e);
			}
		}
		return null;
	}

	@Override
	public Control createOutlineContents(Composite parent, int direction) {
		// hold on to a control that we want to hide:
		return this.outline = (Composite) super.createOutlineContents(parent, direction);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SaveableCompareEditorInput#prepareCompareInput(IProgressMonitor)
	 */
	protected ICompareInput prepareCompareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		ICompareInput input = createCompareInput();
		getCompareConfiguration().setLeftEditable(true);
		getCompareConfiguration().setRightEditable(false);
		ensureContentsCached(this.right, monitor);
		initLabels(input);
		return input;
	}

	private static void ensureContentsCached(BaseMethodCompareElement right, IProgressMonitor monitor) 
	{
		if (right != null) {
			try {
				right.cacheContents(monitor);
			} catch (CoreException e) {
				OTDTUIPlugin.log(e);
			}
		}
	}

	private IResource getResource() {
		return this.roleMethod.getResource();
	}

	private ICompareInput createCompareInput() {
		MyDiffNode input = new MyDiffNode(this.left,this.right);
		return input;
	}

	private void initLabels(ICompareInput input) {
		CompareConfiguration cc = getCompareConfiguration();
		if (this.left != null) {
			String leftLabel = NLS.bind(OTDTUIMessages.CompareBoundMethods_role_method_label, 
									    new Object[]{this.roleMethod.getDeclaringType().getElementName(), this.roleMethod.getElementName()});
			cc.setLeftLabel(leftLabel);
		}
		if (this.right != null) {
			String rightLabel = NLS.bind(OTDTUIMessages.CompareBoundMethods_base_method_label, 
										 new Object[]{this.right.getTypeName(), this.right.getName()});
			cc.setRightLabel(rightLabel);
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		Object[] titleObject = new Object[2];
		titleObject[0] = this.left.getName();
		titleObject[1] = this.right.getName();
		return NLS.bind(OTDTUIMessages.CompareBoundMethods_compare_tooltip, titleObject);	 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {
		Object[] titleObject = new Object[1];
		titleObject[0] = this.left.getName();
		return NLS.bind(OTDTUIMessages.CompareBoundMethods_compare_title, titleObject);	 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IFile.class || adapter == IResource.class) {
			return getResource();
		}
		if (adapter == ISharedDocumentAdapter.class) {
			return getSharedDocumentAdapter();
		}
		return super.getAdapter(adapter);
	}

	private synchronized ISharedDocumentAdapter getSharedDocumentAdapter() {
		if (this.sharedDocumentAdapter == null)
			this.sharedDocumentAdapter = new EditableSharedDocumentAdapter(new EditableSharedDocumentAdapter.ISharedDocumentAdapterListener() {
				public void handleDocumentConnected() { /* ignored */ }
				public void handleDocumentFlushed() {
					IEditorInput input = sharedDocumentAdapter.getDocumentKey(getResource());
					try {
						if (input != null)
							sharedDocumentAdapter.saveDocument(input, true, new NullProgressMonitor());
					} catch (CoreException e) {
						OTDTUIPlugin.log(e);
					}
				}
				public void handleDocumentDeleted() { /* ignored */ }
				public void handleDocumentSaved() { /* ignored */ }
				public void handleDocumentDisconnected() { /* ignored */ }
			});
		return this.sharedDocumentAdapter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.LocalResourceCompareEditorInput#fireInputChange()
	 */
	protected void fireInputChange() {
		((MyDiffNode)getCompareResult()).fireChange();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SaveableCompareEditorInput#contentsCreated()
	 */
	protected void contentsCreated() {
		super.contentsCreated();
		this.outline.setVisible(false);
		this.notifier.initialize();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SaveableCompareEditorInput#handleDispose()
	 */
	protected void handleDispose() {
		super.handleDispose();
		this.notifier.dispose();
		IEditorInput input = this.sharedDocumentAdapter.getDocumentKey(getResource());
		if (input != null)
			this.sharedDocumentAdapter.disconnect(input);
	}	
}
