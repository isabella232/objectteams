/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2010 Stephan Herrmann.
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
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.help.actions;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.objectteams.otdt.ui.IUpdateRulerActionExtender;
import org.eclipse.objectteams.otdt.ui.Messages;
import org.eclipse.objectteams.otdt.ui.help.OTJLDError;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * This class extends the OTDTUI's UpdateRulerAction to provide the "Go to Language Definition" action where appropriate.
 * 
 * @author stephan
 * @since 0.7.0 (Incubation at Eclipse.org)
 */
public class UpdateRulerAction implements IUpdateRulerActionExtender {
	
	public void menuAboutToShow(IMenuManager contextMenu, IDocument document, IEditorPart editor, int line) 
	{
		IAnnotationModel model= JavaUI.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		// this iterator is not protected, it may throw ConcurrentModificationExceptions
		@SuppressWarnings("rawtypes")
		Iterator iter= model.getAnnotationIterator();
		while (iter.hasNext()) {
			Annotation annot= (Annotation) iter.next();
			if (isOTJProblem(annot)) {
				// may throw an IndexOutOfBoundsException upon concurrent annotation model changes
				Position pos= model.getPosition(annot);
				if (pos != null) {
					// may throw an IndexOutOfBoundsException upon concurrent document modification
					try {
						int startLine = document.getLineOfOffset(pos.getOffset());
						if (startLine == line ) {
							installAction(contextMenu, editor, annot.getText());
						}
					} catch (BadLocationException e) {
						// ignore
					} catch (IndexOutOfBoundsException e) {
						// concurrent modification - too bad, ignore
					} catch (ConcurrentModificationException e) {
						// concurrent modification - too bad, ignore
					}
				}
			}
		}
	}

	@SuppressWarnings("restriction") // IJavaAnnotation is internal
	private boolean isOTJProblem(Annotation annot) {
		if (!( annot instanceof IJavaAnnotation))
			return false;
		if (!((IJavaAnnotation) annot).isProblem())
			return false;
		return OTJLDError.isOTJProblem(annot.getText());			
	}
	
	private void installAction(IMenuManager contextMenu, IEditorPart editor, String text) {
		// remove previously inserted action:
		for (IContributionItem item : contextMenu.getItems()) {
			if (item instanceof ActionContributionItem) {
				ActionContributionItem actionItem = (ActionContributionItem) item;
				if (actionItem.getAction().getText().equals(Messages.UpdateRulerAction_goto_otjld_command_label)) {
					contextMenu.remove(actionItem);
					break;
				}
			}
		}
		// add new configured action:
		contextMenu.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, ShowOTJLDAction.createAction(editor.getSite(), text));
	}
}
