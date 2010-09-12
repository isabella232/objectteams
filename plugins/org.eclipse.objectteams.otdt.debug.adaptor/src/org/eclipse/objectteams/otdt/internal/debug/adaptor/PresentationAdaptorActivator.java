/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007, 2010 Technical University Berlin, Germany.
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
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.objectteams.otdt.debug.ui.OTDebugUIPlugin;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.source.Annotation;

import base org.eclipse.debug.internal.ui.InstructionPointerManager;
import base org.eclipse.debug.internal.ui.sourcelookup.SourceLookupFacility;
import base org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;

/**
 * This team watches over a method from JDTModelPresentation
 * to enable adaptation of String representations by PresentationAdaptor,
 * given that the current launch is some OT/J launch.
 * 
 * Also, generally the "__OT__" prefix is filtered from all qualified type names. 
 * 
 * @author stephan
 * @since 1.1.7
 */
@SuppressWarnings("restriction")
public team class PresentationAdaptorActivator
{
	/** Generalized role for different editor-based contexts. */
	protected class EditorBasedAdaptation 
	{
		/** 
		 * When the current editor is a java editor make it available to the
		 * to-be-activated {@link PresentationAdaptor}.
		 * @param textEditor
		 */
		callin void adaptedRun(ITextEditor textEditor)
			when (textEditor instanceof JavaEditor)
		{
			PresentationAdaptor adaptor = PresentationAdaptor.getInstance();
			JavaEditor previous = adaptor.setTextEditor((JavaEditor)textEditor);
			try {
				within(adaptor)
					base.adaptedRun(textEditor);
			} finally {
				adaptor.setTextEditor(previous);
			}			
		}
	}
	/** Let the {@link PresentationAdaptor} adapt positioning of the editor. */
	protected class EditorPositioningAdaptor extends EditorBasedAdaptation playedBy SourceLookupFacility 
	{
		@SuppressWarnings("decapsulation")
		void adaptedRun(ITextEditor editor) 
			<- replace void positionEditor(ITextEditor editor, IStackFrame frame)
			base when (LaunchUtils.isOTLaunch(frame));
	}
	/** Let the {@link PresentationAdaptor} adapt positioning of "current instruction" annotations. */
	protected class AnnotationAdaptor extends EditorBasedAdaptation playedBy InstructionPointerManager
	{
		void adaptedRun(ITextEditor textEditor) 
			<- replace void addAnnotation(ITextEditor textEditor, IStackFrame frame, Annotation annotation)
			base when (LaunchUtils.isOTLaunch(frame));
	}
	/** Let the {@link PresentationAdaptor} adapt composing labels for the debug view. */
	protected class ModelPresentation playedBy JDIModelPresentation 
	{
		String getStackFrameText(IStackFrame frame) <- replace String getStackFrameText(IStackFrame frame)
			base when (LaunchUtils.isOTLaunch(frame));
		callin String getStackFrameText(IStackFrame stackFrame) 
			throws DebugException 
		{
			// while constructing the text for a stack frame use the PresentationAdaptor:
			PresentationAdaptor adaptor = PresentationAdaptor.getInstance();
			String result;
			within (adaptor)
				result = base.getStackFrameText(stackFrame);
			if (stackFrame instanceof JDIStackFrame)
				result = adaptor.postProcess((JDIStackFrame)stackFrame, result);
			return result; 
		}

		Color getForeground(Object element) <- replace Color getForeground(Object element) 
			base when (LaunchUtils.isOTLaunch(element));
		@SuppressWarnings("basecall")
		callin Color getForeground(Object element) {
			if (element instanceof JDIStackFrame) {
				PresentationAdaptor adaptor = PresentationAdaptor.getInstance();
				String colorName= adaptor.getFrameColorName((JDIStackFrame)element);
				if (colorName != null)
					return OTDebugUIPlugin.getPreferenceColor(colorName);
			}
			return base.getForeground(element);
		}
		
		// generally always remove "__OT__" prefixes:
		@SuppressWarnings("decapsulation")
		String beautifyQualifiedName() 
		<- replace String getQualifiedName(String qualifiedName),
				   String getSimpleName(String qualifiedName);

		callin String beautifyQualifiedName() {
			String rawName = base.beautifyQualifiedName();
			return rawName.replaceAll("__OT__", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}		
	}
}
