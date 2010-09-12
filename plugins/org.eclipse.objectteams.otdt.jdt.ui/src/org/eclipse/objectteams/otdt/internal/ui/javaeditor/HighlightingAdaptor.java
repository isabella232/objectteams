/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007  Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: HighlightingAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.javaeditor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.javaeditor.SemanticToken;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.objectteams.otdt.internal.ui.text.OutlineAdaptor;

import base org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightingManager;
import base org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightings.ParameterVariableHighlighting;

/**
 * This aspect fixes syntax highlighting for OT/J code
 *
 * @author stephan
 * @since 0.9.18
 */
@SuppressWarnings("restriction")
public team class HighlightingAdaptor {

	protected class SemanticHighlightingManager playedBy SemanticHighlightingManager
	{
		void myInstall() 
		{
			//{Hack as workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=142299
			OutlineAdaptor.doActivate();
			// SH}
			
		}
		void myInstall() 
			<- after void install(JavaEditor je, JavaSourceViewer sv, IColorManager cm, IPreferenceStore ps);
	}

	/**
 	 * This role brings syntax highlighting to arguments of method specs.
	 */
	@SuppressWarnings("decapsulation")
	protected class ParameterHighLighting playedBy ParameterVariableHighlighting {

		boolean consumes(SemanticToken token) <- replace boolean consumes(SemanticToken token);

		callin boolean consumes(SemanticToken token) {
			if (base.consumes(token))
				return true;
			IBinding binding= token.getBinding();
			if (binding != null && binding.getKind() == IBinding.VARIABLE && !((IVariableBinding) binding).isField()) {
				ASTNode decl= token.getRoot().findDeclaringNode(binding);
				return decl != null && decl.getLocationInParent() == MethodSpec.PARAMETERS_PROPERTY;
			}
			return false;
		}		
	}
}
