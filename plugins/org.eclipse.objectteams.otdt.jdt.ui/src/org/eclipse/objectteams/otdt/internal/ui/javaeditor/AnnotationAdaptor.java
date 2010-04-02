/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AnnotationAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.javaeditor;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.VerticalRulerEvent;
import org.eclipse.objectteams.otdt.internal.ui.javaeditor.RoleOverrideIndicatorManager;
import org.eclipse.swt.widgets.Event;

import base org.eclipse.jdt.internal.ui.javaeditor.JavaSelectAnnotationRulerAction;
import base org.eclipse.jdt.internal.ui.javaeditor.JavaSelectMarkerRulerAction2;

/**
 * Adapt handling of annotations in the (OT)JavaEditor.
 * 
 * 
 * @author stephan
 * @since 1.2.8
 */
@SuppressWarnings("restriction")
public team class AnnotationAdaptor {
	
	/**
	 * This role hooks the RoleOverrideIndicator annotation into the ruler menu managed by
	 * this role's base class.
	 *  
	 * @see RoleOverrideIndicatorManager
	 * 
	 * @author stephan
	 * @since 1.2.8
	 */
	protected class OpenTSuperRole playedBy JavaSelectAnnotationRulerAction {

		void update() <- after void update();

		@SuppressWarnings({ "inferredcallout", "decapsulation" })
		void update() {
			if (fAnnotation instanceof RoleOverrideIndicatorManager.OverrideIndicator) {
				initialize(fBundle, "JavaSelectAnnotationRulerAction.OpenSuperImplementation."); //$NON-NLS-1$
				setEnabled(true);
				return;
			}
		}

		void runWithEvent(Event event) <- after void runWithEvent(Event event);

		@SuppressWarnings("inferredcallout")
		void runWithEvent(Event event) {
			if (fAnnotation instanceof RoleOverrideIndicatorManager.OverrideIndicator) {
				((RoleOverrideIndicatorManager.OverrideIndicator)fAnnotation).open();
				return;
			}
		}
	}
	/** 
	 * If role-over hovers are enabled, we need to hook into one more
	 * action in order to execute our annotation action.
	 */
	protected class RulerAction playedBy JavaSelectMarkerRulerAction2 {

		void annotationDefaultSelected(VerticalRulerEvent event) 
		<- replace void annotationDefaultSelected(VerticalRulerEvent event);

		@SuppressWarnings("basecall")
		callin void annotationDefaultSelected(VerticalRulerEvent event) {
			Annotation annotation = event.getSelectedAnnotation();
			if (annotation instanceof RoleOverrideIndicatorManager.OverrideIndicator)
				((RoleOverrideIndicatorManager.OverrideIndicator) annotation).open();
			else
				base.annotationDefaultSelected(event);
		}
	}
}
