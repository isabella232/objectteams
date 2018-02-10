/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
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
package org.eclipse.objectteams.otdt.internal.ui.assist;

import static org.eclipse.objectteams.otdt.ui.ImageConstants.CALLINMETHOD_IMG;

import java.util.ArrayList;
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
import org.eclipse.jdt.core.dom.AbstractMethodMappingDeclaration;
import org.eclipse.jdt.core.dom.CallinMappingDeclaration;
import org.eclipse.jdt.core.dom.CalloutMappingDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LiftingType;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodMappingElement;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.RoleTypeDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.FixMessages;
import org.eclipse.jdt.internal.corext.fix.IProposableFix;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.fix.Java50CleanUp;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.correction.proposals.AddImportCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.FixCorrectionProposal;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.compiler.ConfigHelper;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.internal.ui.text.correction.MappingProposalSubProcessor;
import org.eclipse.objectteams.otdt.internal.ui.text.correction.TypeProposalSubProcessor;
import org.eclipse.objectteams.otdt.internal.ui.util.Images;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import base org.eclipse.jdt.internal.corext.fix.Java50Fix;
import base org.eclipse.jdt.internal.corext.util.JdtFlags;
import base org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionProcessor;
import base org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor;
import base org.eclipse.jdt.internal.ui.text.correction.QuickFixProcessor;
import base org.eclipse.jdt.internal.ui.text.correction.SuppressWarningsSubProcessor;
import base org.eclipse.jdt.internal.ui.text.correction.UnresolvedElementsSubProcessor;
import base org.eclipse.jdt.internal.ui.text.correction.SuppressWarningsSubProcessor.SuppressWarningsProposal;
import base org.eclipse.jdt.internal.ui.text.correction.proposals.AbstractMethodCorrectionProposal;
import base org.eclipse.jdt.internal.ui.text.correction.proposals.NewMethodCorrectionProposal;

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
		void filterNulls(Collection<ICommandAccess> proposals)
		<-  after void process(IInvocationContext context, IProblemLocation problem, Collection<ICommandAccess> proposals)
			with { proposals <- proposals }

		void filterNulls(Collection<ICommandAccess> proposals) {
			if ((proposals instanceof List)) {
				List<ICommandAccess> list = (List<ICommandAccess>)proposals;
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
		static callin int addSuppressWarningsProposal(ICompilationUnit cu, 
													   ASTNode node, 
													   String warningToken, 
													   int relevance, 
													   Collection<ICommandAccess> proposals) 
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
				return base.addSuppressWarningsProposal(cu, node, warningToken, relevance, proposals);
			}
			String label= NLS.bind(CorrectionMessages.SuppressWarningsSubProcessor_suppress_warnings_label, 
								   warningToken, 
								   name);
			
			// instantiate a role wrapping an invisible class 
			// and immediately lower the object using a public base type:
			ASTRewriteCorrectionProposal proposal=
					new SuppressWarningsProposal(warningToken, label, cu, node, property, relevance);

			proposals.add(proposal);
			return 0; // not affecting a local variable
		}
		
		@SuppressWarnings("decapsulation") // base-side arg "proposals" is not generic 
		addSuppressWarningsProposal <- replace addSuppressWarningsProposalIfPossible; 
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
				if (OTModelManager.isRole(cu.findPrimaryType())) {
					ImportRewrite importRewrite = ((AddImportCorrectionProposal) proposal).getImportRewrite();
					if (importRewrite != null && !importRewrite.myHasRecordedChanges())
						proposal = TypeProposalSubProcessor.createImportInRoFisTeamProposal(cu, fullName, node, relevance, maxProposals);
				}
				
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
					// import for the role side in DeclaredLifting?
					ASTNode typeRef = node.getParent();
					if (typeRef instanceof Type && typeRef.getLocationInParent() == LiftingType.ROLE_TYPE_PROPERTY) {
						return null;
					}
				} catch (JavaModelException jme) {
					// ignore
				}
			}
			return proposal;
		}
		@SuppressWarnings("decapsulation")
		createTypeRefChangeProposal <- replace createTypeRefChangeProposal;
		
		@SuppressWarnings("decapsulation") // Collection
		void addMissingCastParentsProposal(ICompilationUnit cu, MethodInvocation invocationNode) 
			<- replace void addMissingCastParentsProposal(ICompilationUnit cu, MethodInvocation invocationNode, Collection<ICommandAccess> proposals);

		@SuppressWarnings("basecall")
		static callin void addMissingCastParentsProposal(ICompilationUnit cu, MethodInvocation invocationNode) {
			Expression sender = invocationNode.getExpression();
			if (sender != null && sender.getNodeType() == ASTNode.SIMPLE_NAME) {
				if (((SimpleName)sender).getIdentifier().equals(MappingProposalSubProcessor.FAKETHIS))
					return;
				if (((SimpleName)sender).getIdentifier().startsWith(IOTConstants.OT_DOLLAR))
					return;
			}
			base.addMissingCastParentsProposal(cu, invocationNode);
		}
		
	}

	
	/** 
	 * When collecting proposals prepare Config to have at least a stub config.
	 */
	protected class JavaCorrectionProcessor playedBy JavaCorrectionProcessor {
		callin static IStatus guardDependencies() {
			try (ConfigHelper.IConfig config = ConfigHelper.checkCreateStubConfig2(OTQuickFixes.this))
			{
				return base.guardDependencies();
			}
		}
		guardDependencies <- replace collectProposals;
	}
	
	/** Declare a callin binding to a private method. Real stuff happens in sub-role. */
	@SuppressWarnings("abstractrelevantrole")
	protected abstract class AbstractMethodCompletionProposal playedBy AbstractMethodCorrectionProposal 
	{
		@SuppressWarnings("hidden-lifting-problem") // abstract role could potentially throw LiftingFailedException
		void updateRewrite(ASTRewrite rewrite) <- before MethodDeclaration getStub(ASTRewrite rewrite, ASTNode t)
			base when (OTQuickFixes.this.hasRole(base, AbstractMethodCompletionProposal.class));
		abstract void updateRewrite(ASTRewrite rewrite);
	}
	
	/** 
	 * This role helps the MappingProposalSubProcessor for unresolved method specs.
	 * Its behavior is initiated by calling {@link OTQuickFixes#registerNewMethodCorrectionProposal}.
	 * No aspectBinding needed because no inner bound roles.
	 */
	protected team class NewMethodCompletionProposal
		extends AbstractMethodCompletionProposal
		playedBy NewMethodCorrectionProposal
		base when (OTQuickFixes.this.hasRole(base, NewMethodCompletionProposal.class)) // on invitation only
	{
		// === Imports using CALLOUT: ===
		void setImage(Image img)		              				-> void setImage(Image img);
		
		@SuppressWarnings("decapsulation")
		protected String getKEY_TYPE()                				-> get String KEY_TYPE;

		@SuppressWarnings("decapsulation")
		protected void setFArguments(List<Expression> fArguments) 	-> set List<Expression> fArguments;

		ImportRewrite getImportRewrite()                           	-> ImportRewrite getImportRewrite();
		ImportRewrite createImportRewrite(CompilationUnit astRoot) 	-> ImportRewrite createImportRewrite(CompilationUnit astRoot);

		@SuppressWarnings("decapsulation")
		LinkedProposalModel getLinkedProposalModel() 			   	-> LinkedProposalModel getLinkedProposalModel();

		void addLinkedPosition(ITrackedNodePosition position, boolean isFirst, String groupID)
		-> void addLinkedPosition(ITrackedNodePosition position, boolean isFirst, String groupID);

		// === Basic State: ===
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
			if (this.returnType != null) {
				Type newTypeNode = (Type) ASTNode.copySubtree(rewrite.getAST(), this.returnType);
				addLinkedPosition(rewrite.track(newTypeNode), false, getKEY_TYPE());
				return newTypeNode;
			}
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
		
		protected void adjustModifiers(CallinMappingDeclaration callinMapping, boolean isRoleSide) {
			if (isRoleSide && callinMapping.getCallinModifier()==Modifier.OT_REPLACE_CALLIN) {
				this.needCallinModifier= true;
				Image baseImage = JavaPluginImages.get(JavaPluginImages.IMG_MISC_DEFAULT);
				setImage(Images.decorateImage(baseImage, CALLINMETHOD_IMG, IDecoration.TOP_RIGHT));
			}
			if (callinMapping.isStatic())
				this.needStaticModifier= true;
		}

		// ==== Handle lifting/lowering when inferring method signatures across a method mapping: ====

		/** data class for alternative type proposals to be added as linked proposals, once the rewrite is ready. */
		protected class TypeProposalsMemento {
			protected Type originalType;
			protected List<Type> types = new ArrayList<Type>();
			protected String positionGroupID;
			protected TypeProposalsMemento(String positionGroupID, Type originalType) {
				this.positionGroupID = positionGroupID;
				this.originalType = originalType;
			}
		}
		List<TypeProposalsMemento> pendingLinkedLiftings = new ArrayList<TypeProposalsMemento>();

		/**
		 * Propose a type node either directly from typeBinding, 
		 * or considering a lifting/lowering translation if appropriate. 
		 * If translation is indeed proposed, schedule a TypeProposalsMemento
		 * for adding linked proposals once the rewrite is ready.
		 * @param imports     use this for creating type nodes from type bindings
		 * @param ast		  the AST we are generating against
		 * @param typeBinding type binding of AST node to investigate
		 * @param groupKey 	  key of the linked position group
		 * @param roles       available roles in the enclosing team
		 * @param isRoleSide  are we inferring a role method signature?
		 * @return a suitable type node or null
		 */
		protected Type proposeType(ImportRewrite imports, AST ast, ITypeBinding typeBinding, String groupKey, ITypeBinding[] roles, boolean isRoleSide) 
		{
			if (!isRoleSide && typeBinding.isRole()) {
				// if trying to pass one of our roles to the base unconditionally apply lowering
				for (int i = 0; i < roles.length; i++) {
					if (typeBinding.getErasure().equals(roles[i].getErasure())) {
						typeBinding = typeBinding.getBaseClass();
						if (typeBinding == null)
							return null; // cannot lower unbound role
						break;
					}
				}
			}
			Type newType = imports.addImport(typeBinding, ast);
			if (isRoleSide) {
				newType = checkLifting(imports, ast, typeBinding, newType, groupKey, roles);
			}
			return newType;
		}

		/**
		 * check whether a given type is played by a role from a given array and replace the base type with the given role.
		 * However, actual creation of the linked proposal has to be deferred until the rewrite is created by our base.
		 */
		Type checkLifting(ImportRewrite imports, AST ast, ITypeBinding typeBinding, Type originalType, String groupKey, ITypeBinding[] roles)
		{
			Type roleType = null;
			TypeProposalsMemento memento = null;
			for (ITypeBinding roleBinding : roles) {
				if (roleBinding.isSynthRoleIfc()) continue; // synth ifcs would otherwise cause dupes
				if (typeBinding.equals(roleBinding.getBaseClass())) {
					// found
					if (memento == null) {
						memento = new TypeProposalsMemento(groupKey, originalType);
						this.pendingLinkedLiftings.add(memento);
					}
					Type candidateRoleType = imports.addImport(roleBinding, ast);
					memento.types.add(candidateRoleType);
					if (roleType == null) // prefer the first found role type as the primary proposal
						roleType = candidateRoleType;
				}
			}
			if (roleType != null)
				return roleType; // replace with translated type
			return originalType; // unchanged
		}

		void addPendingLiftingProposals(ASTRewrite rewrite) <- after ASTRewrite getRewrite()
			with { rewrite <- result }
		// as the rewrite has been created, add linked proposals now:
		void addPendingLiftingProposals(ASTRewrite rewrite) {
			for (TypeProposalsMemento memento : this.pendingLinkedLiftings) {
				ITrackedNodePosition typePos= rewrite.track(memento.originalType);
				addLinkedPosition(typePos, true, memento.positionGroupID);
				LinkedProposalPositionGroup group=
						getLinkedProposalModel().getPositionGroup(memento.positionGroupID, true);
				for (Type type : memento.types)				
					group.addProposal(type.toString(), null, 13); // TODO: relevance
				group.addProposal(memento.originalType.toString(), null, 13);
			}
		}
	}
	
	/** Register a new method completion proposal with a method spec,
	 *  in order to use the method spec's signature for constructing the new method's signature.
	 * @param spec     the unresolved method spec 
	 * @param proposal a new completion proposal to be adapted
	 */
	@SuppressWarnings("rawtypes")
	public void registerNewMethodCorrectionProposal(MethodSpec spec,
				NewMethodCorrectionProposal as NewMethodCompletionProposal proposal) 
	{
		if (spec.hasSignature()) {
			List parameters= spec.parameters();
			proposal.parameterTypes= new Type[parameters.size()];
			for (int i=0; i<parameters.size(); i++) {
				SingleVariableDeclaration arg= (SingleVariableDeclaration)parameters.get(i);
				proposal.parameterTypes[i]= arg.getType();
			}
			proposal.returnType= spec.getReturnType2();
		} else {
			// create signature from resolved other (base/role) element:
			// Bug 329988 -  Quickfix method generation on missing replace callin method generates wrong method
			StructuralPropertyDescriptor locationInParent = spec.getLocationInParent();
			if (locationInParent == CallinMappingDeclaration.ROLE_MAPPING_ELEMENT_PROPERTY) {
				List baseSpecs = ((CallinMappingDeclaration)spec.getParent()).getBaseMappingElements();
				if (baseSpecs.size() == 1) {
					inferMethodSignature(proposal, spec, ((MethodSpec)baseSpecs.get(0)).resolveBinding(), true);
				}
			} else if (   locationInParent == CallinMappingDeclaration.BASE_MAPPING_ELEMENTS_PROPERTY
					   || locationInParent == CalloutMappingDeclaration.BASE_MAPPING_ELEMENT_PROPERTY)
			{
				MethodSpec roleSpec = (MethodSpec) ((AbstractMethodMappingDeclaration)spec.getParent()).getRoleMappingElement();
				inferMethodSignature(proposal, spec, roleSpec.resolveBinding(), false);
			}
		}
		ASTNode mapping= spec.getParent();
		if (mapping instanceof CallinMappingDeclaration) {
			CallinMappingDeclaration callinDecl = (CallinMappingDeclaration)mapping;
			proposal.adjustModifiers(callinDecl, spec == callinDecl.getRoleMappingElement());
		}
	}
	/** 
	 * A method spec has no signature, infer the signature for a missing method
	 * from the resolved method at the other side of the method mapping.
	 */
	private void inferMethodSignature(NewMethodCompletionProposal proposal, MethodSpec spec, IMethodBinding resolvedMethod, boolean isRoleSide) {
		AST ast = spec.getAST();
		ImportRewrite imports = proposal.getImportRewrite();
		if (imports == null)
			imports = proposal.createImportRewrite((CompilationUnit) ASTResolving.findAncestor(spec, ASTNode.COMPILATION_UNIT));

		ITypeBinding roleTypeBinding = ((RoleTypeDeclaration) ASTResolving.findAncestor(spec, ASTNode.ROLE_TYPE_DECLARATION)).resolveBinding();
		ITypeBinding[] roles= roleTypeBinding.getDeclaringClass().getDeclaredTypes();

		// preset return type and parameter types from the resolved method:
		
		ITypeBinding returnType = resolvedMethod.getReturnType();
		proposal.returnType = proposal.proposeType(imports, ast, returnType, NewMethodCompletionProposal.getKEY_TYPE(), roles, isRoleSide);
		
		ITypeBinding[] parameterTypes = resolvedMethod.getParameterTypes();
		proposal.parameterTypes = new Type[parameterTypes.length];
		List<Expression> arguments = new ArrayList<Expression>(parameterTypes.length);
		for (int i = 0; i < parameterTypes.length; i++) {
			proposal.parameterTypes[i] = proposal.proposeType(imports, ast, parameterTypes[i], "arg_type_"+i, roles, isRoleSide); //$NON-NLS-1$
			arguments.add(ast.newConditionalExpression());  // dummy expr that will be filtered out in StubUtility.getVariableNameSuggestions()
		}
		proposal.setFArguments(arguments); // ensure we will loop properly in NewMethodCorrectionProposal.addNewParameters(ASTRewrite, List, List)
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
		getNeededVisibility <- replace  getNeededVisibility;

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
		protectedRun <- replace addAbstractMethodProposals;

		@SuppressWarnings("basecall")
		static callin void protectedRun() {
			try {
				base.protectedRun();
			} catch (ClassCastException cce) {
				// this one is excepted, base method may try to located abstract method but finds RoleTypeDeclaration
			}
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
