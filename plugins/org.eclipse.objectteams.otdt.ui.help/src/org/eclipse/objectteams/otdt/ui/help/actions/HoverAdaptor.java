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

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.source.Annotation;

import base org.eclipse.jdt.internal.ui.text.java.hover.ProblemHover.ProblemInfo;

/**
 * Add OT-Support to hovers for java problems.
 *  
 * @author stephan
 * @since 0.7.0 (Incubation at Eclipse.org)
 */
@SuppressWarnings({ "restriction", "decapsulation" })
public team class HoverAdaptor {
	
	/** Add the "Go to Language Definition" action to the hover's toolbar. */
	protected class ProblemHoverAdaptor playedBy ProblemInfo {

		void addAction(ToolBarManager manager, Annotation annotation) <- after void fillToolBar(ToolBarManager manager, IInformationControl infoControl)
			base when (isOTJProblem(base.annotation))
			with {  manager    <- manager,
					annotation <- base.annotation }

		void addAction(ToolBarManager manager, Annotation annotation) 
		{
			manager.add(ShowOTJLDAction.createAction(null/*site*/, annotation.getText()));
		}
		
		static boolean isOTJProblem(Annotation annotation) {
			if (annotation instanceof IJavaAnnotation) {
				int problemId = ((IJavaAnnotation) annotation).getId();
				return problemId > IProblem.OTJ_RELATED && problemId < IProblem.TypeRelated;
			}
			return false;
		}
	}
}
