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

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.Member;
import org.eclipse.jdt.internal.debug.ui.DebugUIMessages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.objectteams.otdt.core.compiler.ISMAPConstants;
import org.eclipse.objectteams.otdt.debug.ui.internal.preferences.OTDebugPreferences;

import base org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import base org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import base org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;

/**
 * Adapt the String representation of stack frames in the debug view.
 * 
 * @author stephan
 * @since 1.1.7
 */
@SuppressWarnings("restriction")
public team class PresentationAdaptor 
{
	enum MethodKind {
		PLAIN,
		INITIAL, CHAIN, ORIG, TEAM_WRAPPER, BASE_CALL, // callin related
		LIFT,
		WHEN, BASE_WHEN, // pedicates 
		DECAPS, FIELD_ACCESS, METHOD_BRIDGE, CREATOR, INIT_FIELDS // various generated accesses
	}
	
	@SuppressWarnings("nls")
	public static final String[] enhancementTypes = {
		"org.objectteams.Team[]", "int[]", "int", "int", "int", "java.lang.Object[]"
	};

	// while working for some clients we have an editor that we can use for adapted source lookup:
	private ThreadLocal<JavaEditor> javaEditor = new ThreadLocal<JavaEditor>();
	
	
	private static PresentationAdaptor instance;
	public static PresentationAdaptor getInstance() {
		if (instance == null)
			instance= new PresentationAdaptor();
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	protected class OTJStackFrame playedBy JDIStackFrame 
	{
		// === imports: ===
		boolean isStatic() -> boolean isStatic();
		int modifiers() -> com.sun.jdi.Method getUnderlyingMethod()
			with { result <- result.modifiers() }

		// store analyzed method kind between calls:
		protected MethodKind kind= MethodKind.PLAIN;
		
		// == currently unused: ==
		boolean isRole;
		boolean isTeam; 
		
		protected boolean isPurelyGenerated() {
			switch(this.kind) {
			case LIFT:
			case INITIAL:
			case CHAIN:
			case DECAPS:
			case FIELD_ACCESS:
			case METHOD_BRIDGE:
			case CREATOR:
			case INIT_FIELDS:
				return true;
			}
			return false;
		}
		protected boolean isOTSpecialSrc() {
			switch(this.kind) {
			case TEAM_WRAPPER:
			case BASE_CALL:
			case WHEN:
			case BASE_WHEN:
				return true;
			}
			return false;
		}

		String getMethodName() <- replace String getMethodName();
		@SuppressWarnings("nls")
		callin String getMethodName() throws DebugException {
			String result= base.getMethodName();
			String[] segments= analyzeMethod(result);
			if (segments != null) {
				switch (this.kind) {
				// INITIAL is not yet analyzed
				case ORIG: 
					return segments[1];
				case CHAIN:
					return "{{Dispatch callins for "+segments[1]+"}}";
				case TEAM_WRAPPER:
					return "["+segments[1]+"."+segments[2]+"<-"+segments[3]+"]";
				case LIFT:
					return "{{Lift to "+segments[2]+"}}";
				case BASE_CALL:
					return "base."+segments[1];
				case WHEN:
					return "[when]";
				case BASE_WHEN:
					return "[base when]";
				case DECAPS:
					return "[decapsulation access]";
				case FIELD_ACCESS:
					return "[access to field "+segments[3]+"]";
				case METHOD_BRIDGE:
					return "[access to private role method "+segments[3]+"]";
				case CREATOR:
					return "[access to constructor of role "+segments[2]+"]";
				case INIT_FIELDS:
					return "[initialize role fields]";
				}
			}
			return result;
		}

		/** Analyze the method name and store the kind.
		 * @param methodName
		 * @return an array of segments split at '$'.
		 */
		@SuppressWarnings("nls")
		String[] analyzeMethod(String methodName) 
		{
			if (methodName != null && methodName.startsWith("_OT$")) 
			{
				String[] segments= methodName.split("[$]");
				
				switch (segments.length) {
				case 2:
					if      (segments[1].equals("when"))
						this.kind= MethodKind.WHEN;
					else if (segments[1].equals("base_when"))
						this.kind= MethodKind.BASE_WHEN;
					else if (segments[1].equals("InitFields"))
						this.kind= MethodKind.INIT_FIELDS;
					break;
				case 3:
					if      (segments[2].equals("orig"))   // _OT$bm$orig
						this.kind= MethodKind.ORIG;
					else if (segments[2].equals("chain"))  // _OT$bm$chain
						this.kind= MethodKind.CHAIN;
					else if (segments[1].equals("liftTo")) // _OT$liftTo$R
						this.kind= MethodKind.LIFT;
					else if (segments[1].equals("create")) // _OT$create$R
						this.kind= MethodKind.CREATOR;
					else if (segments[2].equals("base"))   // _OT$rm$base
						this.kind= MethodKind.BASE_CALL;
					break;
				case 4:
					if 		(segments[1].equals("_fieldget_")
						   ||segments[1].equals("_fieldset_"))
						this.kind = MethodKind.FIELD_ACCESS;
					else if (segments[2].equals("private")) // _OT$R$private$m
						this.kind = MethodKind.METHOD_BRIDGE;
					else
						// further analysis needed?
						this.kind= MethodKind.TEAM_WRAPPER;    // _OT$R$rm$bm
				}
				if (segments.length > 1 && segments[1].equals("decaps")) // _OT$decaps$xy.. (even as prefix to other name patterns)
					this.kind = MethodKind.DECAPS;					
				return segments;
			}
			return null;
		}
		
		getArgumentTypeNames <- replace getArgumentTypeNames;
		@SuppressWarnings("rawtypes")
		callin List getArgumentTypeNames() throws DebugException {
			return stripGeneratedParams(base.getArgumentTypeNames());
		}
				
		int getLineNumber() <- replace int getLineNumber();
		callin int getLineNumber() throws DebugException {
			int result= base.getLineNumber();
			if (result >= ISMAPConstants.STEP_INTO_LINENUMBER) {
				if (this.kind == MethodKind.PLAIN) // re-classify, if linenumber was the only unusual property
					this.kind = MethodKind.INITIAL;
				return -1;
			}
			if (this.kind == MethodKind.CHAIN) {
				JavaEditor editor = PresentationAdaptor.this.javaEditor.get();
				if (editor != null) {
					try {
						return editor.getStartLineOfEnclosingElement(result-1)+1; // map between 0/1 based counting.
					} catch (BadLocationException e) {
						OTDebugAdaptorPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, OTDebugAdaptorPlugin.PLUGIN_ID, "Failed to retrieve line number", e)); //$NON-NLS-1$
					}
				}
			}
			return result;
		}
		
		// -- helpers --
		private List<String> stripGeneratedParams(List<String> typeNames) 
				throws DebugException 
		{
			switch (this.kind) {
			case TEAM_WRAPPER:
				if (!isStatic())
					typeNames= typeNames.subList(1, typeNames.size()); // remove role arg
				break;
			case BASE_WHEN:
				typeNames= typeNames.subList(2, typeNames.size()); // remove 2 synth args: dummy,Team
				break;
			}
			// may need to strip multiple sets of enhance-args:
			List<String> stripped= stripEnhancementParams(typeNames);
			while (stripped != typeNames) {
				typeNames= stripped;
				stripped= stripEnhancementParams(typeNames);
			}
// This is now generally done in PresentationAdaptorActivator.ModelPresentation.beautifyQualifiedName()
// Keeping these lines just in case we are now missing some control flows.
//			// go into details: strip __OT__ prefix of individual types
//			for (int i=0; i<stripped.size(); i++)
//				stripped.set(i, beautifyOTTypeName(stripped.get(i)));
			return stripped;
		}
		private List<String> stripEnhancementParams(List<String> typeNames) {
 			if (typeNames != null && typeNames.size() >= 6) {
				for (int i = 0; i < enhancementTypes.length; i++) {
					if (!enhancementTypes[i].equals(typeNames.get(i)))
						return typeNames;
				}
				return typeNames.subList(6, typeNames.size());
			}
			return typeNames;
		}
	}
	/** While assembling the display string (ie., while this team is active) 
	 *  pretend the "OTJ" stratum is "Java", in order to enable Java name assembly.
	 */
	protected class ReferenceType playedBy JDIReferenceType 
	{
		String getDefaultStratum() <- replace String getDefaultStratum();
		callin String getDefaultStratum() throws DebugException {
			String stratum= base.getDefaultStratum();
			if (stratum.equals("OTJ"))                     //$NON-NLS-1$
				return "Java"; // this is where we lie ;-) //$NON-NLS-1$
			return stratum;
		}
	}
	
	/** Gateway to java editors for requesting line numbers using info from the java model. */
	protected class JavaEditor playedBy JavaEditor
	{
		@SuppressWarnings("decapsulation")
		IJavaElement      getElementAt(int offset) -> IJavaElement      getElementAt(int offset);
		IDocumentProvider getDocumentProvider()    -> IDocumentProvider getDocumentProvider();
		IEditorInput      getEditorInput()         -> IEditorInput      getEditorInput();
		
		protected int getStartLineOfEnclosingElement(int line) throws BadLocationException
		{
			IDocument doc = getDocumentProvider().getDocument(getEditorInput());
			IJavaElement element = getElementAt(doc.getLineOffset(line));
			if (!(element instanceof ISourceReference))
				throw new BadLocationException("Element is not an ISourceReference: "+element); //$NON-NLS-1$
			try {
				ISourceRange range = (element instanceof Member)
						? ((Member) element).getNameRange()
						: ((ISourceReference) element).getSourceRange();
				
				return doc.getLineOfOffset(range.getOffset());
			} catch (JavaModelException e) {
				throw new BadLocationException(e.getMessage());
			}
		}
	}
	
	/** Answer the symbolic name of the color that should be used for displaying 
	 *  the given stackframe.
	 * @param element stackframe
	 * @return symbolic color name or null.
	 */
	public String getFrameColorName(JDIStackFrame as OTJStackFrame element) {
		if (element.isPurelyGenerated())
			return OTDebugPreferences.OT_GENERATED_CODE_COLOR;
		if (element.isOTSpecialSrc())
			return OTDebugPreferences.OT_SPECIAL_CODE_COLOR;
		return null;
	}

	/** 
	 * When this team is activated in a JavaEditor-aware-context, remember the java editor.
	 * @param javaEditor new java editor to be remembered, may be null (= reset).
	 * @return previously remembered editor, may be null. 
	 */
	public org.eclipse.jdt.internal.ui.javaeditor.JavaEditor setTextEditor(JavaEditor as JavaEditor javaEditor)
	{
		JavaEditor previous = this.javaEditor.get();
		this.javaEditor.set(javaEditor);
		return previous;
	}

	/** Final embellishment of a label after everything has been analyzed. */
	public String postProcess(JDIStackFrame as OTJStackFrame stackFrame, String labelText) 
	{
		if (stackFrame.kind == MethodKind.INITIAL) // this we didn't know when creating the label text			
			return labelText.replace(DebugUIMessages.JDIModelPresentation_line__76+' '+DebugUIMessages.JDIModelPresentation_not_available, 
							         "[about to enter]"); //$NON-NLS-1$

		return labelText;
	}
}
