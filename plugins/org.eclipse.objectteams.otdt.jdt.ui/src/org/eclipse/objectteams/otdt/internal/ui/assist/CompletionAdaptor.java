/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2008 Technical University Berlin, Germany and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CompletionAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 		Technical University Berlin - Initial API and implementation
 *     	IBM Corporation - copies of individual methods from bound base classes.
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.assist;


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.formatter.IndentManipulation;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.codeassist.impl.Keywords;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.text.java.FieldProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.ui.ImageConstants;
import org.eclipse.objectteams.otdt.ui.ImageManager;

import base org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2;
import base org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import base org.eclipse.jdt.internal.ui.text.java.MethodDeclarationCompletionProposal;
import base org.eclipse.jdt.internal.ui.text.java.MethodProposalInfo;
import base org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import base org.eclipse.jdt.ui.CodeGeneration;
import base org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import base org.eclipse.jdt.ui.text.java.CompletionProposalLabelProvider;
import base org.eclipse.jdt.ui.text.java.JavaTextMessages;

import base org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup.JavaLinkedModeProposal;

/**
 * This team helps the jdt.ui to handle completion for OT-specific elements.
 * Currently:
 * + generate new callout (can be changed to callin in linked mode).
 * 
 * @author stephan
 * 
 * @role MethodMappingCompletionProposal
 * @role CreateMethodMappingCompletionProposal
 * @role CalloutToFieldCompletionProposal
 * @role CallinRHSCompletionProposal
 * @role MethodSpecCompletionProposal
 * @role StubUtility2
 */
@SuppressWarnings({ "restriction", "decapsulation" })
public team class CompletionAdaptor
{
	
	/** 
	 * Relevance factor.
	 * (cf. e.g. {@link org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeRelevance()} 
	 */
	public static int R_METHOD_MAPPING = 16;
	/**
	 * This role defines the entry-hooks of this team. 
	 */
	protected class ProposalCollector playedBy CompletionProposalCollector
	{
		getDeclaringType <- replace getDeclaringType;
		@SuppressWarnings("basecall")
		callin char[] getDeclaringType(CompletionProposal proposal) {
			switch (proposal.getKind()) {
			case CompletionProposal.OT_CALLOUT_DECLARATION:
			case CompletionProposal.OT_CALLOUT_OVERRIDE_DECLARATION:
			case CompletionProposal.OT_CALLOUT_GET:
			case CompletionProposal.OT_CALLOUT_SET:
			case CompletionProposal.OT_CALLIN_DECLARATION:
			case CompletionProposal.OT_FIELD_SPEC:
			case CompletionProposal.OT_METHOD_SPEC:
			case CompletionProposal.OVERRIDE_ROLE_DECLARATION:
				char[] declaration= proposal.getDeclarationSignature();
				return Signature.toCharArray(declaration);
			default:
				return base.getDeclaringType(proposal);
			}
		}
		
		createJavaCompletionProposal <- replace createJavaCompletionProposal;
		@SuppressWarnings("basecall")
		callin IJavaCompletionProposal createJavaCompletionProposal(CompletionProposal proposal) 
		{
			switch (proposal.getKind()) {
			case CompletionProposal.OT_CALLOUT_DECLARATION:
			case CompletionProposal.OT_CALLOUT_OVERRIDE_DECLARATION:
			case CompletionProposal.OT_CALLOUT_GET:
			case CompletionProposal.OT_CALLOUT_SET:				
			case CompletionProposal.OT_CALLIN_DECLARATION:
			case CompletionProposal.OT_METHOD_SPEC:
				return createMappingProposal(proposal, proposal.getKind());
			case CompletionProposal.OT_FIELD_SPEC:
				return createFieldSpecProposal(proposal);
			case CompletionProposal.OVERRIDE_ROLE_DECLARATION:
				return createRoleProposal(proposal);
			case CompletionProposal.KEYWORD:
				IJavaCompletionProposal result = base.createJavaCompletionProposal(proposal);
				if (CharOperation.endsWith(proposal.getCompletion(), Keywords.WHEN)) {
					// move cursor back by one (into '()') by reverse-engineering its current position:
					JavaCompletionProposal jProposal = (JavaCompletionProposal)result;
					Point cursor = jProposal.getSelection(null);
					int cursorPos = cursor.x-jProposal.getReplacementOffset();
					jProposal.setCursorPosition(cursorPos-1);
				}
				return result;
			default:
				return base.createJavaCompletionProposal(proposal);
			}
		}
		
		private IJavaCompletionProposal createRoleProposal(CompletionProposal proposal) {
			// compute label: image ...
			int modifiers = proposal.getFlags();			
			String roleclassImg = Flags.isTeam(modifiers) ? ImageConstants.TEAM_ROLE_IMG : ImageConstants.ROLECLASS_IMG;
			ImageDescriptor baseDesc = ImageManager.getSharedInstance().getDescriptor(roleclassImg);
			Image image= getImage(new JavaElementImageDescriptor(baseDesc, JavaElementImageDescriptor.OVERRIDES, JavaElementImageProvider.SMALL_SIZE));
			// ... and styled string:
			String superTeamName = Signature.toString(String.valueOf(proposal.getDeclarationSignature()));
			StyledString buf= new StyledString();
			buf.append(String.valueOf(proposal.getName()));
			buf.append(" - ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
			buf.append(org.eclipse.objectteams.otdt.internal.ui.Messages.Completion_override_role_label+superTeamName, StyledString.QUALIFIER_STYLER);
			
			return new OverrideRoleCompletionProposal(getCompilationUnit(), proposal, getLength(proposal), buf, image);
		}

		private IJavaCompletionProposal createMappingProposal(CompletionProposal proposal, int kind) 
		{
			if (getCompilationUnit() == null || getJavaProject() == null)
				return null;

			String signature= String.valueOf(proposal.getSignature());
			String[] paramTypes= Signature.getParameterTypes(signature);
			for (int index= 0; index < paramTypes.length; index++)
				paramTypes[index]= Signature.toString(paramTypes[index]);
			int length=       getLength(proposal);
			
			int bindingModifier = proposal.getFlags();
			boolean isCallin= true;
			boolean isOverride= false;
			switch(bindingModifier) {
			case TerminalTokens.TokenNameCALLOUT_OVERRIDE:
				isOverride= true;
				//$FALL-THROUGH$
			case TerminalTokens.TokenNameBINDOUT:
				isCallin= false;
				break;
			// otherwise it's a callin with details in bindingModifier
			}
			
			StyledString label= getLabelProvider().createMappingProposalLabel(proposal, isCallin);
			Image image = getImage(getLabelProvider().createMappingImageDescriptor(bindingModifier));
			
			String fieldType= Signature.getReturnType(signature);
			boolean isSetter= false;
			switch (kind) {
			case CompletionProposal.OT_CALLIN_DECLARATION:
				return new CreateMethodMappingCompletionProposal(getJavaProject(), getCompilationUnit(), proposal,
																 paramTypes, isOverride, /*isOnlyCallin*/true,
																 length, label.toString(), image);
			case CompletionProposal.OT_METHOD_SPEC:
				if (isCallin)
					return new CallinRHSCompletionProposal(getJavaProject(), getCompilationUnit(), proposal, 
														   paramTypes, 
														   length, label.toString(), image);
				else
					return new MethodSpecCompletionProposal(getJavaProject(), getCompilationUnit(), proposal,
															paramTypes,
															length, label.toString(), image);
			case CompletionProposal.OT_CALLOUT_OVERRIDE_DECLARATION:
				isOverride= true;
				//$FALL-THROUGH$
			case CompletionProposal.OT_CALLOUT_DECLARATION:
				return new CreateMethodMappingCompletionProposal(getJavaProject(), getCompilationUnit(), proposal,
												     paramTypes, isOverride, /*isOnlyCallin*/false,
												 	 length, label.toString(), image);
			case CompletionProposal.OT_CALLOUT_SET:
				isSetter= true;
				fieldType= paramTypes[0];
				//$FALL-THROUGH$
			case CompletionProposal.OT_CALLOUT_GET:
				String name= String.valueOf(proposal.getName());
				String fieldName= String.valueOf(Character.toLowerCase(name.charAt(3)));
				if (name.length()>4)
					fieldName += name.substring(4);
				return new CalloutToFieldCompletionProposal(getJavaProject(), getCompilationUnit(), proposal,
												 			fieldName, fieldType, isSetter, isOverride,
												 			length, label.toString(), image);
			default:
				return null;
			}
		}

		/** modeled after {@link org.eclipse.jdt.ui.text.java.CompletionProposalCollector#createFieldProposal} */
		IJavaCompletionProposal createFieldSpecProposal(CompletionProposal proposal) {
			String completion= String.valueOf(proposal.getCompletion());
			int start= proposal.getReplaceStart();
			int length= getLength(proposal);
			LabelProvider labelProvider= getLabelProvider();
  //{ObjectTeams: callout-to-field:
			StyledString label= labelProvider.createFieldDescLabel(proposal);
			Image image= getImage(labelProvider.createMappingImageDescriptor(TerminalTokens.TokenNameBINDOUT));
  // SH}
			int relevance= computeRelevance(proposal);

			JavaCompletionProposal javaProposal= new JavaCompletionProposal(completion, start, length, image, label, relevance, getContext().isInJavadoc(), getInvocationContext());
			if (getJavaProject() != null)
				javaProposal.setProposalInfo(new FieldProposalInfo(getJavaProject(), proposal));
  //{ObjectTeams: slightly different
			javaProposal.setTriggerCharacters(new char[]{' ', '\t', '\n'});
  // SH}

			return javaProposal;
		}


		// CALLOUTS:
		ICompilationUnit getCompilationUnit() -> get ICompilationUnit fCompilationUnit;
		IJavaProject     getJavaProject()     -> get IJavaProject     fJavaProject;
		LabelProvider    getLabelProvider()   -> get CompletionProposalLabelProvider fLabelProvider;
		int getLength(CompletionProposal proposal) -> int getLength(CompletionProposal proposal);
		Image getImage(ImageDescriptor imageDesc)  -> Image getImage(ImageDescriptor imageDesc);
	    IJavaCompletionProposal createMethodReferenceProposal(CompletionProposal methodProposal) 
	    										   -> IJavaCompletionProposal createMethodReferenceProposal(CompletionProposal methodProposal);
	    JavaContentAssistInvocationContext getInvocationContext() 
	    										   -> JavaContentAssistInvocationContext getInvocationContext();
	    CompletionContext getContext()			   -> CompletionContext getContext();
	    
	    protected
	    void unsetIgnored(int kind)                -> void setIgnored(int kind, boolean ignore)
	    	with { kind -> kind, false -> ignore }
	}
	
	/** This role adds new methods to the base-side label provider. */
	protected class LabelProvider playedBy CompletionProposalLabelProvider
	{

		/** Get a callin/callout image (with no decorations yet). */
		protected ImageDescriptor createMappingImageDescriptor(int callinModifier) {
			switch (callinModifier) {
			case 0: // unset but we know it's a callin:
			case TerminalTokens.TokenNamebefore:
				return ImageManager.getSharedInstance().getDescriptor(ImageConstants.CALLINBINDING_BEFORE_IMG);
			case TerminalTokens.TokenNamereplace:
				return ImageManager.getSharedInstance().getDescriptor(ImageConstants.CALLINBINDING_REPLACE_IMG);
			case TerminalTokens.TokenNameafter:
				return ImageManager.getSharedInstance().getDescriptor(ImageConstants.CALLINBINDING_AFTER_IMG);
			case TerminalTokens.TokenNameBINDOUT:
			case TerminalTokens.TokenNameCALLOUT_OVERRIDE:
				return ImageManager.getSharedInstance().getDescriptor(ImageConstants.CALLOUTBINDING_IMG);
			default: 
				return null;
			}
			//  orig from CompletionProposalLabelProvider.createMethodImageDescriptor()
			//final int flags= proposal.getFlags();
			//return decorateImageDescriptor(JavaElementImageProvider.getMethodImageDescriptor(false, flags), proposal);
		}

		@SuppressWarnings("restriction")
		protected
		StyledString createMappingProposalLabel(CompletionProposal proposal, boolean isCallin) {
//{ObjectTeams: OT_COPY_PASTE from CompletionProposalLabelProvider.createOverrideMethodProposalLabel()			
			StyledString nameBuffer= new StyledString();

			// method name
			nameBuffer.append(proposal.getName());

			// parameters
			nameBuffer.append('(');
			appendUnboundedParameterList(nameBuffer, proposal);
			nameBuffer.append(")  "); //$NON-NLS-1$
			
			// return type
			// TODO remove SignatureUtil.fix83600 call when bugs are fixed
			char[] returnType= createTypeDisplayName(SignatureUtil.getUpperBound(Signature.getReturnType(SignatureUtil.fix83600(proposal.getSignature()))));
			nameBuffer.append(returnType);
// SH}
			// different tail:
			nameBuffer.append(" - "); //$NON-NLS-1$
			String message = null;
			switch (proposal.getKind()) {
			case CompletionProposal.OT_CALLOUT_DECLARATION:
			case CompletionProposal.OT_CALLOUT_OVERRIDE_DECLARATION:
			case CompletionProposal.OT_CALLIN_DECLARATION:
				message = org.eclipse.objectteams.otdt.internal.ui.Messages.Completion_method_binding_label;
				break;
			case CompletionProposal.OT_METHOD_SPEC:
				if (isCallin)
					message = org.eclipse.objectteams.otdt.internal.ui.Messages.Completion_callin_label;
				else
					message = org.eclipse.objectteams.otdt.internal.ui.Messages.Completion_callout_label;
				break;
			case CompletionProposal.OT_CALLOUT_GET:
			case CompletionProposal.OT_CALLOUT_SET:
				message = org.eclipse.objectteams.otdt.internal.ui.Messages.Completion_callout_to_field_label;
				break;
			}
			nameBuffer.append(Messages.format(
					message,
					new String(createTypeDisplayName(proposal.getDeclarationSignature()))));
			
			return nameBuffer;
		}
		/* simpler version for callout-to-field RHS: */
		protected StyledString createFieldDescLabel(CompletionProposal proposal) {
			StyledString nameBuffer= new StyledString();
			nameBuffer.append(createJavadocSimpleProposalLabel(proposal));
			nameBuffer.append(" - "); //$NON-NLS-1$
			nameBuffer.append(Messages.format(
							  org.eclipse.objectteams.otdt.internal.ui.Messages.Completion_callout_to_field_label,
							  new String(createTypeDisplayName(proposal.getDeclarationSignature()))));			
			return nameBuffer;
		}
		
		// display nested types including their outer class, relevant for distinguishing tsuper role.
		createOverrideMethodProposalLabel <- replace createOverrideMethodProposalLabel;
		@SuppressWarnings({ "basecall", "inferredcallout" })
		callin StyledString createOverrideMethodProposalLabel(CompletionProposal methodProposal) {
			char[] declaringClass = methodProposal.getDeclarationSignature();
			if (CharOperation.lastIndexOf('$', declaringClass) != -1) {
				StyledString nameBuffer= new StyledString();
				
				// method name
				nameBuffer.append(methodProposal.getName());
				
				// parameters
				nameBuffer.append('(');
				appendUnboundedParameterList(nameBuffer, methodProposal);
				nameBuffer.append(')');

				nameBuffer.append(RETURN_TYPE_SEPARATOR);
				
				// return type
				// TODO remove SignatureUtil.fix83600 call when bugs are fixed
				char[] returnType= createTypeDisplayName(SignatureUtil.getUpperBound(Signature.getReturnType(SignatureUtil.fix83600(methodProposal.getSignature()))));
				nameBuffer.append(returnType);
				
				// declaring type
				nameBuffer.append(QUALIFIER_SEPARATOR, StyledString.QUALIFIER_STYLER);
				
				// SH: this is the only change: neither use FQN nor strip outer class:
				String declaringType= new String(Signature.getSignatureSimpleName(methodProposal.getDeclarationSignature()));
				nameBuffer.append(Messages.format(JavaTextMessages.getResultCollector_overridingmethod(), new String(declaringType)), StyledString.QUALIFIER_STYLER);
				
				return nameBuffer;
			} else {
				return base.createOverrideMethodProposalLabel(methodProposal);
			}
		}
		
		// CALLOUTS:
		StyledString appendUnboundedParameterList(StyledString buffer, CompletionProposal methodProposal)
			-> StyledString appendUnboundedParameterList(StyledString buffer, CompletionProposal methodProposal);

		char[] createTypeDisplayName(char[] typeSignature)
			-> char[] createTypeDisplayName(char[] typeSignature);
		StyledString createJavadocSimpleProposalLabel(CompletionProposal proposal) 
			-> StyledString createJavadocSimpleProposalLabel(CompletionProposal proposal);
	}
	
	// === access to other base classes, no adaptation (yet):
	protected class JavaTextMessages playedBy JavaTextMessages {
		String getResultCollector_overridingmethod() -> get String ResultCollector_overridingmethod;
	}
	
	protected class CalloutProposalInfo playedBy MethodProposalInfo {
		CalloutProposalInfo(IJavaProject project, CompletionProposal proposal) {
			base(project, proposal);
		}
	}
	

	/**
	 * This role mediates between a completion proposal and its import rewrite.
	 * 
	 *  Data (orig): result aka fImportRewrite, fProposal { completionEngine }
	 *  Trigger: checkImportRewrite <- after createImportRewrite (passing result)
	 *  	unconditionally
	 *  Propagate: call BaseImportRewriting.checkForBaseImport()
	 *  	reads: API CompletionEngine.isCompletingBaseclass()
	 *  	writes: Field ImportRewrite.needsBaseImport
	 */
	protected class LazyJavaTypeCompletionProposal playedBy LazyJavaTypeCompletionProposal 
	{
		/** 
		 * Check whether the rewrite should be configured to create base imports.
		 * This is the only situation where proposal and rewrite are seen together.
		 */
		void checkImportRewrite(ImportRewrite rewrite) {
			BaseImportRewriting.instance().checkForBaseImport((InternalCompletionProposal)getProposal(), rewrite);
		}
		void checkImportRewrite(ImportRewrite rewrite) <- after ImportRewrite createImportRewrite()
			with { rewrite <- result }
		
		// callout-to-field:
		CompletionProposal getProposal() -> get CompletionProposal fProposal;
	}
	
	/** 
	 * When proposing a new constructor declaration, do these two adjustments for roles:
	 * <ul>
	 * <li>Strip the <code>__OT__</code> prefix
	 * <li>Insert a base class argument if the role is bound.
	 * </ul> 
	 */
	protected class MethodDeclarationCompletionProposal implements ILowerable playedBy MethodDeclarationCompletionProposal
	{
		// --- callout import ---
		boolean hasMethod(IMethod[] arg0, String arg1) -> boolean hasMethod(IMethod[] arg0, String arg1);
		void setSortString(String arg0)                -> void setSortString(String arg0);
		void setStyledDisplayString(StyledString arg0) -> void setStyledDisplayString(StyledString arg0);
		
		// --- static part ---
		@SuppressWarnings("rawtypes")
		void evaluateProposals(IType type, String prefix, int offset, int length, int relevance, 
							   Set suggestedMethods, Collection resultCollection) 
		<- replace 
		void evaluateProposals(IType type, String prefix, int offset, int length, int relevance,
		 					   Set suggestedMethods, Collection resultCollection);

		@SuppressWarnings({ "unchecked", "basecall", "rawtypes" })
		static callin void evaluateProposals(IType type, String prefix, int offset, int length, int relevance, 
											 Set suggestedMethods, Collection result) 
				 throws CoreException
		{
			IOTType ottype = OTModelManager.getOTElement(type);
			if (ottype == null || !ottype.isRole()) {
				base.evaluateProposals(type, prefix, offset, length, relevance, suggestedMethods, result); 
				return; 
			}
			IRoleType roleType = (IRoleType) ottype;
			IMethod[] methods= type.getMethods();
			if (!type.isInterface()) {
				String typeName = type.getElementName();
				String constructorName=  typeName.startsWith(IOTConstants.OT_DELIM)
											? typeName.substring(IOTConstants.OT_DELIM_LEN) // strip __OT__
											: typeName;
				String baseClassName = roleType.getBaseclassName();
				boolean hasMethod = (baseClassName != null) 
									 ? hasBoundRoleCtor(methods, typeName, baseClassName)   //different check for existence of lifting ctor
									 : hasMethod(methods, constructorName);
				if (constructorName.length() > 0 && constructorName.startsWith(prefix) && !hasMethod && suggestedMethods.add(constructorName))
					result.add(new MethodDeclarationCompletionProposal(type, constructorName, baseClassName, null, offset, length, relevance + 500)
									.lower());
			}
			if (prefix.length() > 0 && !"main".equals(prefix) && !hasMethod(methods, prefix) && suggestedMethods.add(prefix)) { //$NON-NLS-1$
				if (!JavaConventionsUtil.validateMethodName(prefix, type).matches(IStatus.ERROR))
					result.add(new MethodDeclarationCompletionProposal(type, prefix, null, Signature.SIG_VOID, offset, length, relevance)
									.lower());
			}
		}
		private static boolean hasBoundRoleCtor(IMethod[] methods, String roleName, String baseName) {
			baseName = baseName+';';
			for (int i= 0; i < methods.length; i++) {
				IMethod curr= methods[i];
				if (curr.getElementName().equals(roleName) && curr.getParameterTypes().length == 1) { // expect one param: base. 
					String currParamType = curr.getParameterTypes()[0];
					switch (currParamType.charAt(0)) {
					case Signature.C_RESOLVED:
					case Signature.C_UNRESOLVED:
						if (currParamType.substring(1).equals(baseName))
							return true;
					}
				}
			}
			return false;
		}
		
		// --- instance part ---
		
		String constructorSignature = null;

		protected MethodDeclarationCompletionProposal(IType type, String constructorName, String baseClassName, String returnTypeSig, int offset, int length, int i) {
			base(type, constructorName, returnTypeSig, offset, length, i);
			if (baseClassName != null) {
				this.constructorSignature = makeLiftingCtorSignature(baseClassName);
				StyledString displayName = makeLiftingCtorDisplayName(constructorName, this.constructorSignature);
				this.setStyledDisplayString(displayName);
				this.setSortString(displayName.toString());
			}
		}

		StyledString makeLiftingCtorDisplayName(String constructorName, String constructorSignature) {
			StyledString buf= new StyledString();
			buf.append(constructorName);
			buf.append(constructorSignature);
			buf.append(" - ", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
			buf.append(org.eclipse.objectteams.otdt.internal.ui.Messages.Completion_default_lifting_constructor_label, StyledString.QUALIFIER_STYLER);
			return buf;
		}
		String makeLiftingCtorSignature(String baseClassName) {
			StringBuffer buf= new StringBuffer();
			buf.append('(');
			buf.append(baseClassName);
			buf.append(' ');
			buf.append(baseClassName.toLowerCase().charAt(0));
			if (baseClassName.length() > 1)
				buf.append(baseClassName.substring(1));
			buf.append(')');
			return buf.toString();
		}
		
		/** replacement computation is hardwired, fixup the signature afterwards. */
		void replaceCtorSignature(String replacementString) <- replace void setReplacementString(String replacementString)
			when (this.constructorSignature != null);
		callin void replaceCtorSignature(String replacementString) {
			base.replaceCtorSignature(replacementString.replace("()", this.constructorSignature));//$NON-NLS-1$
		} 
	}

	/** 
	 * This role together with {@link SuperCallAdjustor} arranges that new method declarations
 	 * overriding a tsuper version get a proper tsuper-call as their default body. 
	 */
	protected class OverrideCompletionProposal playedBy OverrideCompletionProposal 
	{
		updateReplacementString <- replace updateReplacementString;
	
		@SuppressWarnings({"basecall", "inferredcallout"}) // copy&paste from base method, OT modifications are marked
		callin boolean updateReplacementString(IDocument document, char trigger, int offset, ImportRewrite importRewrite) 
				throws CoreException, BadLocationException 
		{
			// OT_COPY_PASTE:

			Document recoveredDocument= new Document();
			CompilationUnit unit= getRecoveredAST(document, offset, recoveredDocument);
			ImportRewriteContext context;
			if (importRewrite != null) {
				context= new ContextSensitiveImportRewriteContext(unit, offset, importRewrite);
			} else {
				importRewrite= StubUtility.createImportRewrite(unit, true); // create a dummy import rewriter to have one
				context= new ImportRewriteContext() { // forces that all imports are fully qualified
					public int findInContext(String qualifier, String name, int kind) {
						return RES_NAME_CONFLICT;
					}
				};
			}

			ITypeBinding declaringType= null;
			ChildListPropertyDescriptor descriptor= null;
			ASTNode node= NodeFinder.perform(unit, offset, 0);
			if (node instanceof AnonymousClassDeclaration) {
				declaringType= ((AnonymousClassDeclaration) node).resolveBinding();
				descriptor= AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY;
			} else if (node instanceof AbstractTypeDeclaration) {
				AbstractTypeDeclaration declaration= (AbstractTypeDeclaration) node;
				descriptor= declaration.getBodyDeclarationsProperty();
				declaringType= declaration.resolveBinding();
			}
			if (declaringType != null) {
				ASTRewrite rewrite= ASTRewrite.create(unit.getAST());
				IMethodBinding methodToOverride= Bindings.findMethodInHierarchy(declaringType, fMethodName, fParamTypes);
				if (methodToOverride == null && declaringType.isInterface()) {
					methodToOverride= Bindings.findMethodInType(node.getAST().resolveWellKnownType("java.lang.Object"), fMethodName, fParamTypes); //$NON-NLS-1$
				}
				if (methodToOverride != null) {
//{ObjectTeams:
				  methodToOverride = adjustToClassPart(methodToOverride, declaringType);
				  try {
					if (isTSuperOf(methodToOverride.getDeclaringClass(), declaringType))
						CompletionAdaptor.enableSuperCallAdjustor.set(Boolean.TRUE);
// orig:
					CodeGenerationSettings settings= JavaPreferencesSettings.getCodeGenerationSettings(fJavaProject);
					MethodDeclaration stub= org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2.createImplementationStub(fCompilationUnit, rewrite, importRewrite, context, methodToOverride, declaringType.getName(), settings, declaringType.isInterface());
					ListRewrite rewriter= rewrite.getListRewrite(node, descriptor);
					rewriter.insertFirst(stub, null);

					ITrackedNodePosition position= rewrite.track(stub);
					try {
						rewrite.rewriteAST(recoveredDocument, fJavaProject.getOptions(true)).apply(recoveredDocument);

						String generatedCode= recoveredDocument.get(position.getStartPosition(), position.getLength());
						int generatedIndent= IndentManipulation.measureIndentUnits(getIndentAt(recoveredDocument, position.getStartPosition(), settings), settings.tabWidth, settings.indentWidth);

						String indent= getIndentAt(document, getReplacementOffset(), settings);
						setReplacementString(IndentManipulation.changeIndent(generatedCode, generatedIndent, settings.tabWidth, settings.indentWidth, indent, TextUtilities.getDefaultLineDelimiter(document)));

					} catch (MalformedTreeException exception) {
						JavaPlugin.log(exception);
					} catch (BadLocationException exception) {
						JavaPlugin.log(exception);
					}
// :giro
				  } finally {
					  CompletionAdaptor.enableSuperCallAdjustor.set(null);
				  }
// SH}
				}
			}
			return true;
		}
		private IMethodBinding adjustToClassPart(IMethodBinding method, ITypeBinding declaringType) {
			ITypeBinding methodDeclaringType = method.getDeclaringClass();
			if (methodDeclaringType != declaringType && methodDeclaringType.isRole() && methodDeclaringType.isInterface()) {
				ITypeBinding teamType = methodDeclaringType.getDeclaringClass();
				if (teamType != null) {
					String key = method.getKey();
					int pos = key.indexOf('.');
					String shortKey = key.substring(pos);
					// find corresponding role class:
					for (ITypeBinding member : teamType.getDeclaredTypes()) {
						if (member.isClass() && member.getName().equals(declaringType.getName())){
							// find corresponding method in role class:
							for (IMethodBinding classMethod : member.getDeclaredMethods())
								if (classMethod.getKey().endsWith(shortKey)) 
									return classMethod;
							break;
						}
					}
				}

			}
			return method;
		}
		private boolean isTSuperOf(ITypeBinding potentialTSuper, ITypeBinding declaringType) {
			if (!potentialTSuper.isRole() || !declaringType.isRole())
				return false;
			for (ITypeBinding tsuperType : declaringType.getSuperRoles())
				if (   potentialTSuper == tsuperType 
					|| isTSuperOf(potentialTSuper, tsuperType))
					return true;
			return false;
		}
	}

	// this thread local flag passes activation from OverrideCompletionProposal to SuperCallAdjustor
	final static ThreadLocal<Boolean> enableSuperCallAdjustor = new ThreadLocal<Boolean>();

	/** Enabled by {@link OverrideCompletionProposal} this role adjusts super calls to tsuper calls. */
	protected class SuperCallAdjustor playedBy CodeGeneration 
		base when (CompletionAdaptor.enableSuperCallAdjustor.get() != null)
	{
		static callin void getMethodBodyContent(boolean isConstructor, String bodyStatement) throws CoreException 
		{
			String pattern = "super."; //$NON-NLS-1$
			String replacement = "tsuper."; //$NON-NLS-1$
			if (isConstructor) {
				pattern = "super("; //$NON-NLS-1$
				replacement = "tsuper("; //$NON-NLS-1$
			}
			bodyStatement = bodyStatement.replaceFirst(pattern, replacement);
			base.getMethodBodyContent(isConstructor, bodyStatement);
		}
		void getMethodBodyContent(boolean isConstructor, String bodyStatement) 
		<- replace String getMethodBodyContent(ICompilationUnit cu, String declaringTypeName, String methodName, boolean isConstructor, String bodyStatement, String lineDelimiter)
		   with { isConstructor <- isConstructor, bodyStatement <- bodyStatement }
	}

	int computeRelevance(CompletionProposal proposal) {
		return proposal.getRelevance() * R_METHOD_MAPPING;
	}
	
	/** find insertion position, and insert: */
	@SuppressWarnings("rawtypes") // DOM-lists
	void insertStub(ASTRewrite                  rewrite, 
					ASTNode                     node, 
					ChildListPropertyDescriptor bodyProperty,
					int                         position,
					ASTNode                     stub) 
	{
		ListRewrite bodyRewrite= rewrite.getListRewrite(node, bodyProperty);
		List bodyDecls= (List)node.getStructuralProperty(bodyProperty);

		ASTNode prev= null;
		if (bodyDecls != null && !bodyDecls.isEmpty()) {
			for (Iterator iterator = bodyDecls.iterator(); iterator.hasNext();) {
				ASTNode cur= (ASTNode) iterator.next();
				if (cur.getStartPosition()<position)
					prev= cur;
				else 
					break;
			}
		}
		if (prev != null)
			bodyRewrite.insertAfter(stub, prev, null);
		else
			bodyRewrite.insertFirst(stub, null);
	}
}
