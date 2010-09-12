/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CodeManipulationAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 		Technical University Berlin - Initial API and implementation
 *     	IBM Corporation - copies of individual methods from bound base classes.
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.corext;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccessSpec;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodSpec;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;

import base org.eclipse.jdt.internal.corext.codemanipulation.GetterSetterUtil;
import base org.eclipse.jdt.internal.corext.codemanipulation.ImportReferencesCollector;
import base org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import base org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.TypeReferenceProcessor;
import base org.eclipse.jdt.internal.corext.dom.ScopeAnalyzer;


/**
 * @author stephan
 */
@SuppressWarnings({"restriction","decapsulation"})
public team class CodeManipulationAdaptor 
{
	protected class TypeReferenceProcessor playedBy TypeReferenceProcessor 
	{
		ScopeAnalyzer fAnalyzer() -> get ScopeAnalyzer fAnalyzer;
		
		needsImport <- replace needsImport;
		@SuppressWarnings({ "basecall", "unchecked" })
		callin boolean needsImport(ITypeBinding typeBinding, SimpleName ref)
		{
			// OT_COPY_PASTE:

//{ObjectTeams: check if we need to apply OT-specific strategy:
			boolean isTeam = false;
			// cache:
			ScopeAnalyzer analyzer = fAnalyzer();
			List<AbstractTypeDeclaration> types  = analyzer.fRoot().types();
			for (AbstractTypeDeclaration type : types) {
				if (Modifier.isTeam(type.getModifiers())) {
					isTeam = true;
					break;
				}					
			}
			if (!isTeam) // simply use the original version:
				return base.needsImport(typeBinding, ref);
// SH}
			
			if (!typeBinding.isTopLevel() && !typeBinding.isMember()) {
				return false; // no imports for anonymous, local, primitive types or parameters types
			}
// disable visibility issues for OT:
//			int modifiers= typeBinding.getModifiers();
//			if (Modifier.isPrivate(modifiers)) {
//				return false; // imports for privates are not required
//			}
			ITypeBinding currTypeBinding= Bindings.getBindingOfParentType(ref);
			if (currTypeBinding == null) {
				return false; // not in a type
			}
// disable for OT:
//			if (!Modifier.isPublic(modifiers)) {
//				if (!currTypeBinding.getPackage().getName().equals(typeBinding.getPackage().getName())) {
//					return false; // not visible
//				}
//			}
			
			ASTNode parent= ref.getParent();
			while (parent instanceof Type) {
				parent= parent.getParent();
			}
			if (parent instanceof AbstractTypeDeclaration && parent.getParent() instanceof CompilationUnit) {
				return true;
			}
			
			if (typeBinding.isMember()) {
//{ObjectTeams:  never import a role
				if (typeBinding.isRole())
					return false;
// SH}
				// use cached instance:
				if (analyzer.isDeclaredInScope(typeBinding, ref, analyzer.TYPES | analyzer.CHECK_VISIBILITY))
					return false;
			}
			return true;				
		}

		isOfKind <- replace isOfKind;

		@SuppressWarnings("basecall")
		callin boolean isOfKind(TypeNameMatch curr) {
			// a role type is NEVER a good guess for a missing import.
			if (Flags.isRole(curr.getModifiers()))
				return false;
			return base.isOfKind(curr);
		}
		
	}
	
	protected class ScopeAnalyzer playedBy ScopeAnalyzer 
	{
		// expose constants as role fields:
		protected int TYPES;
		protected int CHECK_VISIBILITY;
		
		ScopeAnalyzer(ScopeAnalyzer b) {
			TYPES = getTYPES();
			CHECK_VISIBILITY = getCHECK_VISIBILITY();
		}
		
	  protected
		boolean isDeclaredInScope(IBinding declaration, SimpleName selector, int flags) -> boolean isDeclaredInScope(IBinding declaration, SimpleName selector, int flags);
	  protected
		CompilationUnit fRoot()   -> get CompilationUnit fRoot;
	  // internal:
		int getTYPES()            -> get int TYPES;
		int getCHECK_VISIBILITY() -> get int CHECK_VISIBILITY;
	}
	/**
	 * Make sure that method specs never trigger static imports of their
	 * references methods/fields. 
	 */
	protected class ImportReferencesCollector playedBy ImportReferencesCollector {
		boolean inMethodSpec= false;
		void setMethodSpec(boolean in) {
			this.inMethodSpec= in;
		}
		void setMethodSpec(boolean in) <- after boolean visit(MethodSpec spec)
			with { in <- true }
		void setMethodSpec(boolean in) <- before void endVisit(MethodSpec spec)
			with { in <- false}
		void setMethodSpec(boolean in) <- after boolean visit(FieldAccessSpec spec)
			with { in <- true }
		void setMethodSpec(boolean in) <- before void endVisit(FieldAccessSpec spec)
			with { in <- false}
		
		@SuppressWarnings("basecall")
		callin void possibleStaticImportFound() {
			// nop
		}
		possibleStaticImportFound <- replace possibleStaticImportFound
			when (this.inMethodSpec);
	}
	
	/** 
	 * This role advises the OrganizeImportsOperation to create base imports for role files, too.
	 */
	protected class OrganizeImports playedBy OrganizeImportsOperation {

		@SuppressWarnings("unchecked")
		void collectRoFiBaseReferences(CompilationUnit astRoot, List<SimpleName> typeReferences) 
			<- after boolean collectReferences(CompilationUnit astRoot, List typeReferences, List staticReferences, Set oldSingleImports, Set oldDemandImports)
			base when (result);

		void collectRoFiBaseReferences(CompilationUnit astRoot, List<SimpleName> typeReferences) {
			for (String baseName : RoleFileAdaptor.getRoFiBaseClassNames(astRoot))
				typeReferences.add(newTypeRef(astRoot, baseName));
		}

		SimpleName newTypeRef(CompilationUnit astRoot, String baseName) {
			AST ast = astRoot.getAST();
			SimpleName ref = ast.newSimpleName(baseName);
			// wrap in a type literal so that downstream will reckognize this as a type reference
			TypeLiteral wrapper = ast.newTypeLiteral();
			wrapper.setType(ast.newSimpleType(ref));
			return ref;
		}
	}
	
	/**
	 * This role ensures nobody tries to create getters/setters for internal fields like _OT$base.
	 * This is done by generating an unexpected name which will be filtered out 
	 * e.g. by GetterSetterCompletionProposal#evaluateProposals(). 
	 */
	protected class GetterSetterUtil playedBy GetterSetterUtil 
	{
		// this name does NOT start with "get" as expected.
		final static String NOT_A_VALID_NAME = "_OT$notValidName"; //$NON-NLS-1$

		String getGetterSetterName(IField field) <- replace 
			String getGetterName(IField field, String[] excludedNames),
			String getSetterName(IField field, String[] excludedNames);

		@SuppressWarnings("basecall")
		static callin String getGetterSetterName(IField field) throws JavaModelException {
			if (field.getElementName().startsWith(IOTConstants.OT_DOLLAR)) {
				return NOT_A_VALID_NAME;
			}
			return base.getGetterSetterName(field);
		}
	}
}
