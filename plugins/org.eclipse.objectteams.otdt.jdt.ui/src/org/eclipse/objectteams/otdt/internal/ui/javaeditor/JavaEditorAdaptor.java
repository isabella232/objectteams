/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JavaEditorAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.javaeditor;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.ui.javaeditor.RoleOverrideIndicatorManager;

import base org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import base org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import base org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import base org.eclipse.jdt.internal.ui.text.CombinedWordRule.WordMatcher;


/**
 * This team adapts the JDT Java Editor for OT/J.
 * It replaces the former OTJavaEditor, OTJavaTextTools and OTJavaSourceViewerConfiguration classes.
 * 
 * @author mosconi
 * @since 1.3.0
 */
@SuppressWarnings({"restriction", "inferredcallout", "decapsulation"})
public team class JavaEditorAdaptor {

	/** 
	 * For source and class file editors: figure out whether the input is OT source of plain Java.
	 */
	protected class JavaEditor playedBy JavaEditor 
	{
		/** 
		 * True if we can positively tell that the editor's input contains
 		 * a valid non-OT java type (we default to the OT-behavior).
		 */
		protected boolean isSourcePlainJava = false;
		
		/** We need the editor input to figure out OT vs. Java source. */
		protected void analyzeInput(IEditorInput input) {
			ITypeRoot typeRoot = JavaUI.getEditorInputTypeRoot(input);
			if (typeRoot != null) {
				IType primaryType = typeRoot.findPrimaryType();
				if (primaryType != null)
					isSourcePlainJava = !OTModelManager.hasOTElementFor(primaryType);
			}
		}
		void analyzeInput(IEditorInput input) <- before void doSetInput(IEditorInput input);		
	}
	protected class CompilationUnitEditor extends JavaEditor playedBy CompilationUnitEditor {
		// this instance manages the override indicator for role classes:
		RoleOverrideIndicatorManager fRoleOverrideIndicatorManager;
		
		uninstallOverrideIndicator <- after uninstallOverrideIndicator;
		protected void uninstallOverrideIndicator() {
			if (fRoleOverrideIndicatorManager != null) {
				fRoleOverrideIndicatorManager.removeAnnotations();
				fRoleOverrideIndicatorManager = null;
			}
		}
		
		installOverrideIndicator <- after installOverrideIndicator
			when (!isSourcePlainJava);
		protected void installOverrideIndicator(boolean provideAST) {
			// verbatim repeat:
			IAnnotationModel model = getDocumentProvider().getAnnotationModel(getEditorInput());
			final ITypeRoot inputElement = getInputJavaElement();
			if (model == null || inputElement == null)
				return;
			//own:
			fRoleOverrideIndicatorManager = new RoleOverrideIndicatorManager(model, inputElement, null);

			if (provideAST) {
				CompilationUnit ast = SharedASTProvider.getAST(inputElement, SharedASTProvider.WAIT_ACTIVE_ONLY, getProgressMonitor());
				fRoleOverrideIndicatorManager.reconciled(ast, true, getProgressMonitor());
			}
			addReconcileListener(fRoleOverrideIndicatorManager);
		}
	}

	final static String LAST_JAVA_KEYWORD = "while"; //$NON-NLS-1$
	final static String WITHIN_KEYWORD    = "within"; //$NON-NLS-1$
	@SuppressWarnings("nls")
	final static String[] OTJ_KEYWORDS = {
		WITHIN_KEYWORD, "base", "tsuper", "callin", "playedBy", "with", "team", "as",
		"result", "replace", "after", "before", "when", "get", "set", "precedence"
	};
	
	protected team class JavaSourceViewerConfiguration playedBy JavaSourceViewerConfiguration 
	{
		callin void initializeScanners() {
			within (this)
				base.initializeScanners();
		}
		initializeScanners <- replace initializeScanners;

		/** Fetch the editor role, but only if base side type is compatible. */
		JavaEditor getEditor() -> ITextEditor getEditor()
			with { result <- (JavaEditor)(result instanceof JavaEditor ? result : null) }
		
		/** Is the editor configured for plain Java source? */
		protected boolean isSourcePlainJava() {
			JavaEditor editor = getEditor();
			return editor != null && editor.isSourcePlainJava;
		}
		
		/** Add the OT/J keywords to the matcher. This role is only active during initializeScanners(). */
		protected class WordMatcher playedBy WordMatcher {
			
			void addWord(String word, IToken token) <- replace void addWord(String word, IToken token);
			
			@SuppressWarnings("basecall") // multiple
			callin void addWord(String word, IToken token) {
				base.addWord(word, token);
				if (LAST_JAVA_KEYWORD.equals(word)) {
					if (JavaSourceViewerConfiguration.this.isSourcePlainJava())
						base.addWord(WITHIN_KEYWORD, token); // in plain java mode "within" is our only keyword
					else
						for (int i=0; i<OTJ_KEYWORDS.length; i++)
							base.addWord(OTJ_KEYWORDS[i], token);
				}
			}
		}
	}
}
