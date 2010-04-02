/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AnnotationHelper.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.callinmarkers;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jdt.internal.ui.javaeditor.JavaAnnotationIterator;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * This class helps the callin marker creator to handle problem annotations/markers.
 * 
 * @author stephan
 * @since 1.2.6
 */
public class AnnotationHelper {
	private AnnotationModel fAnnotationModel;

	public AnnotationHelper(IEditorPart targetEditor, IEditorInput editorInput) {
		ITextEditor textEditor = (ITextEditor) targetEditor.getAdapter(ITextEditor.class);
		if (textEditor != null) {
			IDocumentProvider provider= textEditor.getDocumentProvider();
			fAnnotationModel = (AnnotationModel) provider.getAnnotationModel(editorInput);
		}
	}
    
    /** If a callout binds to an unused private method/field, remove the "unused" warning. */
    <M> void removeSomeWarnings(IResource resource, ISourceRange nameRange) throws CoreException 
    {
    	if (fAnnotationModel == null)
    		return;
    	
		Iterator annotationIterator = new JavaAnnotationIterator(
						fAnnotationModel.getAnnotationIterator(nameRange.getOffset(), nameRange.getLength(), false, true),
						false); // not all, only problems
		boolean needRemoveMarker = false;
	annotations: 
		while (annotationIterator.hasNext()) {
			Object next = annotationIterator.next();
			if (next instanceof IJavaAnnotation) {
				IJavaAnnotation javaAnnot = (IJavaAnnotation) next;
				if (javaAnnot.isProblem() || isProblemMarkerAnnotation(javaAnnot))
				{
					switch (javaAnnot.getId()) {
					case IProblem.UnusedPrivateField:
					case IProblem.UnusedPrivateMethod:
						fAnnotationModel.removeAnnotation((Annotation)javaAnnot); // remove from the ruler
						if (javaAnnot instanceof MarkerAnnotation) {
							((MarkerAnnotation) javaAnnot).getMarker().delete();  // remove from problems view
						} else {
							needRemoveMarker = true;
						}
						break annotations;
					}
				}
			}
		}
		if (!needRemoveMarker)
			return;
		IMarker[] problems = resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_ZERO);
		if (problems != null) {
			markers: for (IMarker problem : problems) {
				int problemStart = problem.getAttribute(IMarker.CHAR_START, -1);
				if (problemStart >= nameRange.getOffset() && problemStart < (nameRange.getOffset()+nameRange.getLength()))
				{
					switch(problem.getAttribute(IJavaModelMarker.ID, -1)) {
					case IProblem.UnusedPrivateField:
					case IProblem.UnusedPrivateMethod:
						problem.delete();
						break markers;
					}
				}
			}
		}
    }
    
	private static boolean isProblemMarkerAnnotation(IJavaAnnotation annotation) {
		if (!(annotation instanceof MarkerAnnotation))
			return false;
		try {
			return(((MarkerAnnotation)annotation).getMarker().isSubtypeOf(IMarker.PROBLEM));
		} catch (CoreException e) {
			return false;
		}
	}
	
}
