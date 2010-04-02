/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: OTQuickFixes.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.assist;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodMappingElement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.FixMessages;
import org.eclipse.jdt.internal.corext.fix.IProposableFix;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.fix.Java50CleanUp;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.AddImportCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.CUCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.FixCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

import static org.eclipse.objectteams.otdt.ui.ImageConstants.CALLINMETHOD_IMG;

import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.compiler.ConfigHelper;
import org.eclipse.objectteams.otdt.internal.ui.text.correction.MappingProposalSubProcessor;
import org.eclipse.objectteams.otdt.internal.ui.text.correction.TypeProposalSubProcessor;
import org.eclipse.objectteams.otdt.internal.ui.util.Images;

import base org.eclipse.jdt.internal.corext.fix.Java50Fix;
import base org.eclipse.jdt.internal.corext.util.JdtFlags;
import base org.eclipse.jdt.internal.ui.text.correction.proposals.AbstractMethodCorrectionProposal;
import base org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionProcessor;
import base org.eclipse.jdt.internal.ui.text.correction.proposals.NewMethodCorrectionProposal;
import base org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor;
import base org.eclipse.jdt.internal.ui.text.correction.QuickFixProcessor;
import base org.eclipse.jdt.internal.ui.text.correction.SuppressWarningsSubProcessor;
import base org.eclipse.jdt.internal.ui.text.correction.UnresolvedElementsSubProcessor;
import base org.eclipse.jdt.internal.ui.text.correction.SuppressWarningsSubProcessor.SuppressWarningsProposal;

/**
 * This team class extends the quickfix functionality of the jdt.ui for OT/J elements.
 * Currently supported:
 * <ul>
 * <li>inserting @SuppressWarnings for callin & callout bindings 
 *     ({@link OTQuickFixes.SuppressWarningsAdaptor},{@link OTQuickFixes.SuppressWarningsProposal}).</li>
 * <li>Proposing imports for role types is intercepted to propose an anchored type instead
 *     {@link OTQuickFixes.UnresolvedElementsSubProcessor}).</li>
 * <li>Adapt JavaCorrectionProcessor to cope with entries into the compiler without
 *     a Config configured ({@link OTQuickFixes.JavaCorrectionProcessor}).</li>
 * <li>Create new methods from an unresolved method spec ({@link OTQuickFixes.NewMethodCompletionProposal}).</li>
 * <li>Modifier corrections for role methods and ctors ({@link OTQuickFixes.ModifierCorrectionSubProcessor}).</li>
 * </ul>
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public team class OTQuickFixes  {
	
	private static OTQuickFixes instance;
	
	public OTQuickFixes() {
		instance= this;
	}
	public static OTQuickFixes instance() {
		return instance;
	}
	
	/**
	 * Filter null proposals (we might have suppressed some proposals by nullification)
	 */
	protected class QuickFixProcessor playedBy QuickFixProcessor 
	{
		@SuppressWarnings("unchecked")
		void filterNulls(Collection proposals)
		<-  after void process(IInvocationContext context, IProblemLocation problem, Collection proposals)
			with { proposals <- proposals }

		@SuppressWarnings("unchecked")
		void filterNulls(Collection proposals) {
			if ((proposals instanceof List)) {
				List list = (List)proposals;
				for (int i=list.size()-1; i>=0; i--)
					if (list.get(i) == null)
						list.remove(i);
			}
		}
	}

	/** 
	 * This role adapts the processor responsible for generating SuppressWarnings-proposals. 
	 */
	protected class SuppressWarningsAdaptor playedBy SuppressWarningsSubProcessor
	{
		@SuppressWarnings("basecall")  
		static callin void addSuppressWarningsProposal(ICompilationUnit cu, 
													   ASTNode node, 
													   String warningToken, 
													   int relevance, 
													   Collection<ASTRewriteCorrectionProposal> proposals) 
		{
			// adding one case block to the front of the original method:
			ChildListPropertyDescriptor property= null;
			String name;
			Object baseElement;
			switch (node.getNodeType()) {
			case ASTNode.CALLIN_MAPPING_DECLARATION:
				property= CallinMappingDeclaration.MODIFIERS2_PROPERTY;
				baseElement = ((CallinMappingDeclaration) node).getBaseMappingElements().get(0);
				name = ((CallinMappingDeclaration) node).getRoleMappingElement().getName().getIdentifier();
				name += "<-"; //$NON-NLS-1$
				name += ((MethodSpec)baseElement).getName().getIdentifier();
				break;			
			case ASTNode.CALLOUT_MAPPING_DECLARATION:
				property= CalloutMappingDeclaration.MODIFIERS2_PROPERTY;
				baseElement = ((CalloutMappingDeclaration) node).getBaseMappingElement();
				name = ((CalloutMappingDeclaration) node).getRoleMappingElement().getName().getIdentifier();
				name += "->"; //$NON-NLS-1$
				name += ((MethodMappingElement)baseElement).getName().getIdentifier();
				break;			
			case ASTNode.ROLE_TYPE_DECLARATION:
				property= RoleTypeDeclaration.MODIFIERS2_PROPERTY;
				name= ((RoleTypeDeclaration) node).getName().getIdentifier();
				break;
			default: 
				// other cases are already handled by the original method.
				base.addSuppressWarningsProposal(cu, node, warningToken, relevance, proposals);
				return;
			}
			String label= Messages.format(
								CorrectionMessages.SuppressWarningsSubProcessor_suppress_warnings_label, 
								new String[] { warningToken, name });
			
			// instantiate a role wrapping an invisible class 
			// and immediately lower the object using a public base type:
			ASTRewriteCorrectionProposal proposal= OTQuickFixes.this.
					new SuppressWarningsProposal(warningToken, label, cu, node, property, relevance);

			proposals.add(proposal);
		}
		
		@SuppressWarnings({ "unchecked", "decapsulation" }) // base-side arg "proposals" is not generic 
		addSuppressWarningsProposal <- replace addSuppressWarningsProposal; 
	}
	
	/**
	 * The sole purpose of this role class is providing access to an invisible base class.
	 */	
	@SuppressWarnings("decapsulation") // base class and its constructor
	protected class SuppressWarningsProposal playedBy SuppressWarningsProposal
	{
		public SuppressWarningsProposal(String warningToken, String label, ICompilationUnit cu, ASTNode node, ChildListPropertyDescriptor property, int relevance) 
		{
			base(warningToken, label, cu, node, property, relevance);
		}
	}

	/**
	 * Prevent proposing an import for a role type (which would be illegal!), 
	 * but instead delegate to TypeProposalSubProcessor.changeTypeToAnchored().
	 * 
	 * Prevent proposing a cast for the faked $fakethis$ name.
	 */
	protected class UnresolvedElementsSubProcessor playedBy UnresolvedElementsSubProcessor 
	{
		static callin CUCorrectionProposal createTypeRefChangeProposal(ICompilationUnit cu, String fullName, Name node, int relevance, int maxProposals)
		{
			CUCorrectionProposal proposal = base.createTypeRefChangeProposal(cu, fullName, node, relevance, maxProposals);
			if (proposal instanceof AddImportCorrectionProposal) 
			{
				// if importRewrite suppressed a base import in a role file, don't propose empty changes, but redirect to the team:
				ImportRewrite importRewrite = ((AddImportCorrectionProposal) proposal).getImportRewrite();
				if (importRewrite != null && !importRewrite.myHasRecordedChanges())
					proposal = TypeProposalSubProcessor.createImportInRoFisTeamProposal(cu, fullName, node, relevance, maxProposals);
				
				// we cannot import roles, check if it is a role:
				try {
					IType type = cu.getJavaProject().findType(fullName);
					if (type != null) {
						IOTType otType = OTModelManager.getOTElement(type);
						if (otType != null && otType.isRole()) {
							// but an anchored role type needs no import: propose to add an anchor:
							return TypeProposalSubProcessor.changeTypeToAnchored(cu, fullName, node, ((IRoleType)otType).getTeam().getFullyQualifiedName());
						}
					}
				} catch (JavaModelException jme) {
					// ignore
				}
			}
			return proposal;
		}
		@SuppressWarnings("decapsulation")
		createTypeRefChangeProposal <- replace createTypeRefChangeProposal;
		
		@SuppressWarnings({ "rawtypes", "decapsulation" }) // Collection
		void addMissingCastParentsProposal(ICompilationUnit cu, MethodInvocation invocationNode) 
			<- replace void addMissingCastParentsProposal(ICompilationUnit cu, MethodInvocation invocationNode, Collection proposals);

		@SuppressWarnings("basecall")
		static callin void addMissingCastParentsProposal(ICompilationUnit cu, MethodInvocation invocationNode) {
			Expression sender = invocationNode.getExpression();
			if (sender != null && sender.getNodeType() == ASTNode.SIMPLE_NAME) 
				if (((SimpleName)sender).getIdentifier().equals(MappingProposalSubProcessor.FAKETHIS))
					return;
			base.addMissingCastParentsProposal(cu, invocationNode);
		}
		
	}

	
	/** 
	 * When collecting proposals prepare Config to have at least a stub config.
	 */
	protected class JavaCorrectionProcessor playedBy JavaCorrectionProcessor {
		callin static IStatus guardDependencies() {
			boolean hasConfig = ConfigHelper.checkCreateStubConfig(OTQuickFixes.this);
			try {
				return base.guardDependencies();
			} finally {
				if (hasConfig)
					ConfigHelper.removeConfig(OTQuickFixes.this);
			}
		}
		guardDependencies <- replace collectProposals;
	}
	
	/** Declare a callin binding to a private method. Real stuff happens in sub-role. */
	@SuppressWarnings("abstractrelevantrole")
	protected abstract class AbstractMethodCompletionProposal playedBy AbstractMethodCorrectionProposal 
	{
		void updateRewrite(ASTRewrite rewrite) <- before MethodDeclaration getStub(ASTRewrite rewrite, ASTNode t)
			base when (OTQuickFixes.this.hasRole(base, AbstractMethodCompletionProposal.class));
		abstract void updateRewrite(ASTRewrite rewrite);
	}
	
	/** 
	 * This role helps the MappingProposalSubProcessor for unresolved method specs.
	 * Its behavior is initiated by calling {@link OTQuickFixes#registerNewMethodCorrectionProposal}.
	 */
	protected class NewMethodCompletionProposal
		extends AbstractMethodCompletionProposal
		playedBy NewMethodCorrectionProposal
		base when (OTQuickFixes.this.hasRole(base, NewMethodCompletionProposal.class)) // on invitation only
	{
		/** Actual types etc. from the method spec. */
		protected Type[] parameterTypes;
		protected Type returnType;
		protected boolean needCallinModifier;
		protected boolean needStaticModifier;
		
		@SuppressWarnings("basecall")
		callin Type substituteParameterType(AST ast, Expression elem, String key) 
		{
			final String prefix = "arg_type_";  //$NON-NLS-1$
			if (this.parameterTypes != null && key.startsWith(prefix)) {
				int idx= Integer.parseInt(key.substring(prefix.length()));
				if (idx < this.parameterTypes.length)
					return (Type) ASTNode.copySubtree(ast, this.parameterTypes[idx]);
			}
			return base.substituteParameterType(ast, elem, key);
		}
		@SuppressWarnings("decapsulation")
		substituteParameterType <- replace evaluateParameterType;
		
		@SuppressWarnings("basecall")
		callin Type getMethodReturnType (ASTRewrite rewrite) throws CoreException {
			if (this.returnType != null)
				return (Type) ASTNode.copySubtree(rewrite.getAST(), this.returnType);
			return base.getMethodReturnType(rewrite);
		}
		getMethodReturnType <- replace getNewMethodType; 
		
		callin int evaluateModifiers(ASTNode targetTypeDecl) {
			int result= base.evaluateModifiers(targetTypeDecl);
			if (this.needCallinModifier) {
				result &= ~(Modifier.PRIVATE|Modifier.PROTECTED|Modifier.PUBLIC);
				result |= Modifier.OT_CALLIN;
			}
			if (this.needStaticModifier) 
				result |= Modifier.STATIC;
			return result;
		}
		@SuppressWarnings("decapsulation")
		int evaluateModifiers(ASTNode targetTypeDecl) 
		<- replace int evaluateModifiers(ASTNode targetTypeDecl);
		
		@Override
		void updateRewrite(ASTRewrite rewrite) {
			if (this.needCallinModifier)
				rewrite.setToOTJ();
		}
		
		protected void adjustModifiers(CallinMappingDeclaration callinMapping) {
			if (callinMapping.getCallinModifier()==Modifier.OT_REPLACE_CALLIN) {
				this.needCallinModifier= true;
				Image baseImage = JavaPluginImages.get(JavaPluginImages.IMG_MISC_DEFAULT);
				setImage(Images.decorateImage(baseImage, CALLINMETHOD_IMG, IDecoration.TOP_RIGHT));
			}
			if (callinMapping.isStatic())
				this.needStaticModifier= true;
		}

		void setImage(Image img)		-> void setImage(Image img);
	}
	
	/** Register a new method completion proposal with a method spec,
	 *  in order to use the method specs signature for constructing the new method's signature.
	 * @param spec     the unresolved method spec 
	 * @param proposal a new completion proposal to be adapted
	 */
	@SuppressWarnings("unchecked")
	public void registerNewMethodCorrectionProposal(MethodSpec spec,
				NewMethodCorrectionProposal as NewMethodCompletionProposal proposal) 
	{
		List parameters= spec.parameters();
		proposal.parameterTypes= new Type[parameters.size()];
		for (int i=0; i<parameters.size(); i++) {
			SingleVariableDeclaration arg= (SingleVariableDeclaration)parameters.get(i);
			proposal.parameterTypes[i]= arg.getType();
		}
		proposal.returnType= spec.getReturnType2();
		ASTNode mapping= spec.getParent();
		if (mapping instanceof CallinMappingDeclaration)
			proposal.adjustModifiers((CallinMappingDeclaration)mapping);
	}
	
	/** 
	 * This flag lets clients control whether modifiers should unconditionally be set to <code>public</code>.
	 * Note that this static variable is not thread-safe. 
	 */
	public static boolean publicRequested= false;
	/**
	 * Adapt proposals for non-accessible references using a {@link QuickFixCoreAdaptor}.
	 * Here: adjust the needed visibility for role members.
	 */
	protected class ModifierCorrectionSubProcessor playedBy ModifierCorrectionSubProcessor {

		cflow <- replace addNonAccessibleReferenceProposal;

		static callin void cflow() throws CoreException {
			within (new QuickFixCoreAdaptor())
				base.cflow();
		}

		@SuppressWarnings("decapsulation")
		int getNeededVisibility(ASTNode currNode, ITypeBinding targetType) 
			<- replace  int getNeededVisibility(ASTNode currNode, ITypeBinding targetType);

		@SuppressWarnings("basecall")
		callin static int getNeededVisibility(ASTNode currNode, ITypeBinding targetType) {
			if (publicRequested) return Modifier.PUBLIC;
			int vis = base.getNeededVisibility(currNode, targetType);
			if (vis == 0 && targetType.isRole()) {
				// is targetType in scope from currNode? TODO(SH): this search is not exact.
				ITypeBinding currentEnclosing= Bindings.getBindingOfParentType(currNode);
				while (currentEnclosing != null) {
					ITypeBinding targetEnclosing = targetType.getDeclaringClass();
					while (targetEnclosing != null) {
						if (targetEnclosing.equals(currentEnclosing))
							return Modifier.PROTECTED;
						targetEnclosing = targetEnclosing.getDeclaringClass();
					}
					currentEnclosing = currentEnclosing.getDeclaringClass();
				}
				// accessing from the outside, requires public:
				return Modifier.PUBLIC;
			}
			return vis;
		}	
	}
	
	/** Interpret some flags with knowledge of OT/J: */
	protected class FlagAdaptation playedBy JdtFlags {

		@SuppressWarnings("decapsulation")
		boolean isInterfaceOrAnnotationMember(IBinding binding) 
			<- replace boolean isInterfaceOrAnnotationMember(IBinding binding);

		static callin boolean isInterfaceOrAnnotationMember(IBinding binding) {
			boolean result = base.isInterfaceOrAnnotationMember(binding);
			if (result) { // double check for member of synth ifc:
				ITypeBinding declaringType= null;
				if (binding instanceof IVariableBinding) {
					declaringType= ((IVariableBinding) binding).getDeclaringClass();
				} else if (binding instanceof IMethodBinding) {
					declaringType= ((IMethodBinding) binding).getDeclaringClass();
				} else if (binding instanceof ITypeBinding) {
					declaringType= ((ITypeBinding) binding).getDeclaringClass();
				}
				if (declaringType != null && declaringType.isSynthRoleIfc())
					return false; // result was false alarm ;-)
			}
			return result;
		}
		
	}
	
	/**
	 * Quickfixes for things that are new in Java 5, here: annotations.
	 * <ul>
	 * <li>Support @Override annotation for role classes, too.</li>
	 * </ul>
	 * @since 1.2.8
	 */
	protected class Java50Fix playedBy Java50Fix {

		@SuppressWarnings("decapsulation")
		protected Java50Fix createFix(CompilationUnit compilationUnit, IProblemLocation problem, String annotation, String label) 
			   -> Java50Fix createFix(CompilationUnit compilationUnit, IProblemLocation problem, String annotation, String label);

		@SuppressWarnings("decapsulation")
		ASTNode getDeclaringNode(ASTNode selectedNode) <- replace ASTNode getDeclaringNode(ASTNode selectedNode);

		/** Also expect RoleTypeDeclaration. */
		static callin ASTNode getDeclaringNode(ASTNode selectedNode) {
			ASTNode result = base.getDeclaringNode(selectedNode);
			if (result != null)
				return result;
			// similar to fragment in base method:
			if (selectedNode instanceof SimpleName) {
				StructuralPropertyDescriptor locationInParent= selectedNode.getLocationInParent();
				if (locationInParent == RoleTypeDeclaration.NAME_PROPERTY)
					return selectedNode.getParent();
			}
			return null;
		}		
	}
	
	/**
	 * Offer proposal from ModifierCorrectionSubProcessor in a version adapted for role classes instead of methods. 
	 * @param context   completion context to pass through
	 * @param problem   the problem that triggered this assist
	 * @param proposals list of proposals to which the new proposal should be added.
	 */
	@SuppressWarnings("unchecked")
	public void addOverrideAnnotationProposal(IInvocationContext context, IProblemLocation problem, Collection proposals) {
		// avoid calling Java50Fix.createAddOverrideAnnotationFix, which expects a specific problemID
		IProposableFix fix= Java50Fix.createFix(context.getASTRoot(), problem, "Override", FixMessages.Java50Fix_AddOverride_description); //$NON-NLS-1$
		// original from ModifierCorrectionSubProcessor:
		if (fix != null) {
			Image image= JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
			Map options= new Hashtable();
			options.put(CleanUpConstants.ADD_MISSING_ANNOTATIONS, CleanUpOptions.TRUE);
			options.put(CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE, CleanUpOptions.TRUE);
			FixCorrectionProposal proposal= new FixCorrectionProposal(fix, new Java50CleanUp(options), 5, image, context);
			proposals.add(proposal);
		}
	}
}
