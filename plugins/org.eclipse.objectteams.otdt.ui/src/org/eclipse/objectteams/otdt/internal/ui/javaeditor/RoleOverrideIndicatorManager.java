/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Technical University Berlin - Initial API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.javaeditor;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSelectAnnotationRulerAction;
import org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;

/**
 * Manages the override indicators for the given Java element and annotation model.
 * 
 * Copied from {@link org.eclipse.jdt.internal.ui.javaeditor.OverrideIndicatorManager} and slightly adjusted.
 * 
 * Note that inaccessible features preclude that this class were directly integrated into
 * {@link JavaSelectAnnotationRulerAction}, which is why the jdt.ui.adaptor has to
 * help (see {@link org.eclipse.objectteams.otdt.internal.ui.javaeditor.AnnotationAdaptor}).
 *
 * @author stephan
 * @since 1.2.8
 */
public class RoleOverrideIndicatorManager implements IJavaReconcilingListener {

	/**
	 * Role class override indicator annotation.
	 */
	public class OverrideIndicator extends Annotation {

		private String fAstNodeKey;

		/**
		 * Creates a new override annotation.
		 *
		 * @param text the text associated with this annotation
		 * @param key the type binding key
		 */
		OverrideIndicator(String text, String key) {
			super(ANNOTATION_TYPE, false, text);
			fAstNodeKey= key;
		}

		/**
		 * Opens and reveals the defining role (closest tsuper).
		 */
		public void open() {
			CompilationUnit ast= SharedASTProvider.getAST(fJavaElement, SharedASTProvider.WAIT_ACTIVE_ONLY, null);
			if (ast != null) {
				ASTNode node= ast.findDeclaringNode(fAstNodeKey);
//{ObjectTeams: specific search strategy for a role's tsupers:				
				if (node instanceof TypeDeclaration) {
					try {
						ITypeBinding typeBinding = ((TypeDeclaration)node).resolveBinding();
						ITypeBinding[] tsuperRoles = typeBinding.getSuperRoles();
						if (tsuperRoles != null && tsuperRoles.length > 0) {
							IJavaElement definingRole = tsuperRoles[0].getJavaElement();
							if (definingRole!= null) {
// SH}
								JavaUI.openInEditor(definingRole, true, true);
								return;
							}
						}
					} catch (CoreException e) {
						ExceptionHandler.handle(e, OTJavaEditorMessages.RoleOverrideIndicator_open_error_title, OTJavaEditorMessages.RoleOverrideIndicator_open_error_messageHasLogEntry);
						return;
					}
				}
			}
			String title= OTJavaEditorMessages.RoleOverrideIndicator_open_error_title;
			String message= OTJavaEditorMessages.RoleOverrideIndicator_open_error_message;
			MessageDialog.openError(JavaPlugin.getActiveWorkbenchShell(), title, message);
		}
	}

	static final String ANNOTATION_TYPE= "org.eclipse.objectteams.otdt.ui.roleOverrideIndicator"; //$NON-NLS-1$

	private IAnnotationModel fAnnotationModel;
	private Object fAnnotationModelLockObject;
	private Annotation[] fOverrideAnnotations;
	private ITypeRoot fJavaElement;


	public RoleOverrideIndicatorManager(IAnnotationModel annotationModel, ITypeRoot javaElement, CompilationUnit ast) {
		Assert.isNotNull(annotationModel);
		Assert.isNotNull(javaElement);

		fJavaElement= javaElement;
		fAnnotationModel=annotationModel;
		fAnnotationModelLockObject= getLockObject(fAnnotationModel);

		updateAnnotations(ast, new NullProgressMonitor());
	}

	/**
	 * Returns the lock object for the given annotation model.
	 *
	 * @param annotationModel the annotation model
	 * @return the annotation model's lock object
	 */
	private Object getLockObject(IAnnotationModel annotationModel) {
		if (annotationModel instanceof ISynchronizable) {
			Object lock= ((ISynchronizable)annotationModel).getLockObject();
			if (lock != null)
				return lock;
		}
		return annotationModel;
	}

	/**
	 * Updates the override annotations based on the given AST.
	 *
	 * @param ast the compilation unit AST
	 * @param progressMonitor the progress monitor
	 */
	protected void updateAnnotations(CompilationUnit ast, IProgressMonitor progressMonitor) {

		if (ast == null || progressMonitor.isCanceled())
			return;

		final Map<OverrideIndicator, Position> annotationMap= new HashMap<OverrideIndicator, Position>(10);

		ast.accept(new ASTVisitor(false) {
			/*
			 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.RoleTypeDeclaration)
			 */
			public boolean visit(RoleTypeDeclaration node) {
				ITypeBinding binding= node.resolveBinding();
				if (binding != null) {
//{ObjectTeams: different search for super implementation:
					ITypeBinding[] definingRoles= binding.getSuperRoles();
					if (definingRoles != null && definingRoles.length > 0) {
						ITypeBinding definingRole = definingRoles[0];
// SH}
						ITypeBinding definingType= definingRole.getDeclaringClass();
						String qualifiedRoleName= definingType.getQualifiedName() + "." + binding.getName(); //$NON-NLS-1$

						String text= MessageFormat.format(OTJavaEditorMessages.RoleOverrideIndicator_overrides, new Object[] {BasicElementLabels.getJavaElementName(qualifiedRoleName)});

						SimpleName name= node.getName();
						Position position= new Position(name.getStartPosition(), name.getLength());

						annotationMap.put(new OverrideIndicator(text, binding.getKey()), position);
					}
				}
				return true;
			}
		});

		if (progressMonitor.isCanceled())
			return;

		synchronized (fAnnotationModelLockObject) {
			if (fAnnotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension)fAnnotationModel).replaceAnnotations(fOverrideAnnotations, annotationMap);
			} else {
				removeAnnotations();
				Iterator<Map.Entry<OverrideIndicator, Position>> iter= annotationMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<OverrideIndicator, Position> mapEntry= iter.next();
					fAnnotationModel.addAnnotation(mapEntry.getKey(), mapEntry.getValue());
				}
			}
			fOverrideAnnotations= annotationMap.keySet().toArray(new Annotation[annotationMap.keySet().size()]);
		}
	}

	/**
	 * Removes all override indicators from this manager's annotation model.
	 */
	//FIXME: made public to workaround IllegalAccessError with split package access 
	//       from bundle org.eclipse.objectteams.otdt.jdt.ui(x-friends not working)
	public void removeAnnotations() {
		if (fOverrideAnnotations == null)
			return;

		synchronized (fAnnotationModelLockObject) {
			if (fAnnotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension)fAnnotationModel).replaceAnnotations(fOverrideAnnotations, null);
			} else {
				for (int i= 0, length= fOverrideAnnotations.length; i < length; i++)
					fAnnotationModel.removeAnnotation(fOverrideAnnotations[i]);
			}
			fOverrideAnnotations= null;
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener#aboutToBeReconciled()
	 */
	public void aboutToBeReconciled() {
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener#reconciled(CompilationUnit, boolean, IProgressMonitor)
	 */
	public void reconciled(CompilationUnit ast, boolean forced, IProgressMonitor progressMonitor) {
		updateAnnotations(ast, progressMonitor);
	}
}

