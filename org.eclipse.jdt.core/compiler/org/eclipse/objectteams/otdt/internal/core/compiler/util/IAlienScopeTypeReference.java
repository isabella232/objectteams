/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2008, 2011 Technical University Berlin, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: IAlienScopeTypeReference.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import org.eclipse.jdt.internal.compiler.CompilationResult.CheckPoint;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Supertype for various type references that should be resolved using
 * an alien scope, here: the scope from which the reference has been copied.
 *
 * @author stephan
 * @since 1.2.1 (before that the implementing classes were anonymous classes)
 */
public interface IAlienScopeTypeReference {
	Scope getAlienScope();
}

	// ===== And now for some implementing classes: =====

	class AlienScopeSingleTypeReference extends SingleTypeReference implements IAlienScopeTypeReference
	{
		Scope alienScope;
		public AlienScopeSingleTypeReference(char[] source, long pos, Scope alienScope) {
			super(source, pos);
			this.alienScope = alienScope;
		}
		public Scope getAlienScope() { return this.alienScope; }
		@Override
		public TypeBinding checkResolveUsingBaseImportScope(Scope scope, boolean tolerate) {
			return super.checkResolveUsingBaseImportScope(this.alienScope, tolerate);
		}
		@Override
		public TypeBinding resolveType(ClassScope scope) {
			// `scope` may be stronger then `alienScope`, try it first:
			// (see 1.1.27-otjld-constructor-of-nested-team-2)
			TypeDeclaration referenceContext = scope.referenceContext;
			CheckPoint cp = null;
			if (referenceContext != null)
				cp = referenceContext.compilationResult().getCheckPoint(referenceContext);
			TypeBinding result= super.resolveType(scope);
			if (result != null && result.isValidBinding())
				return result;
			// remove problem binding if any:
			this.resolvedType = null;
			if (cp != null && referenceContext != null) // 2. check redundant via correlation
				referenceContext.compilationResult.rollBack(cp);
			return super.resolveType(this.alienScope.classScope());
		}
		@Override
		public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
			// this variant for use within callin wrappers:
			return super.resolveType((BlockScope) this.alienScope, checkBounds);
		}
	}

	class AlienScopeArrayTypeReference extends ArrayTypeReference implements IAlienScopeTypeReference
	{
		Scope alienScope;
		public AlienScopeArrayTypeReference(char[] source, long pos, int dim, Scope alienScope) {
			super(source, dim, pos);
			this.alienScope = alienScope;
		}
		public Scope getAlienScope() { return this.alienScope; }
		@Override
		public TypeBinding checkResolveUsingBaseImportScope(Scope scope, boolean tolerate) {
			return super.checkResolveUsingBaseImportScope(this.alienScope, tolerate);
		}
		@Override
		public TypeBinding resolveType(ClassScope scope) {
			// `scope` may be stronger then `alienScope`, try it first:
			// (see 1.1.27-otjld-constructor-of-nested-team-2)
			TypeDeclaration referenceContext = scope.referenceContext;
			CheckPoint cp = null;
			if (referenceContext != null)
				cp = referenceContext.compilationResult().getCheckPoint(referenceContext);
			TypeBinding result= super.resolveType(scope);
			if (result != null && result.isValidBinding())
				return result;
			// remove problem binding if any:
			this.resolvedType = null;
			if (cp != null && referenceContext != null) // 2. check redundant via correlation
				referenceContext.compilationResult.rollBack(cp);
			return super.resolveType(this.alienScope.classScope());
		}
		@Override
		public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
			// this variant for use within callin wrappers:
			return super.resolveType((BlockScope) this.alienScope, checkBounds);
		}
	}

	// exactly like AlienScopeSingleTypeReference, but different super class
	class AlienScopeParameterizedSingleTypeReference extends ParameterizedSingleTypeReference implements IAlienScopeTypeReference
	{
		Scope alienScope;
		public AlienScopeParameterizedSingleTypeReference(char[] source, TypeReference[] typeArguments, int dimensions, long pos, Scope alienScope) {
			super(source, typeArguments, dimensions, pos);
			this.alienScope = alienScope;
		}
		public Scope getAlienScope() { return this.alienScope; }
		@Override
		public TypeBinding checkResolveUsingBaseImportScope(Scope scope, boolean tolerate) {
			// `scope` may be stronger then `alienScope`, try it first:
			// (see 1.1.22-otjld-layered-teams-5)
			TypeBinding result= super.checkResolveUsingBaseImportScope(scope, tolerate);
			if (result != null && result.isValidBinding())
				return result;
			// remove problem binding if any:
			this.resolvedType = null;
			return super.checkResolveUsingBaseImportScope(this.alienScope, tolerate);
		}
		@Override
		public TypeBinding resolveType(ClassScope scope) {
			// `scope` may be stronger then `alienScope`, try it first:
			// (see 1.1.27-otjld-constructor-of-nested-team-2)
			TypeDeclaration referenceContext = scope.referenceContext;
			CheckPoint cp = null;
			if (referenceContext != null)
				cp = referenceContext.compilationResult().getCheckPoint(referenceContext);
			TypeBinding result= super.resolveType(scope);
			if (result != null && result.isValidBinding())
				return result;
			// remove problem binding if any:
			this.resolvedType = null;
			if (cp != null && referenceContext != null) // 2. check redundant via correlation
				referenceContext.compilationResult.rollBack(cp);
			return super.resolveType(this.alienScope.classScope());
		}
		@Override
		public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
			// this variant for use within callin wrappers:
			return super.resolveType((BlockScope) this.alienScope, checkBounds);
		}
	}
	
	class AlienScopeQualifiedTypeReference extends QualifiedTypeReference implements IAlienScopeTypeReference
	{
		Scope alienScope;
		public AlienScopeQualifiedTypeReference(char[][] sources, long[] poss, Scope alienScope) {
			super(sources, poss);
			this.alienScope = alienScope;
			this.bits |= ASTNode.IsGenerated; // allow qualified reference to role
		}
		public Scope getAlienScope() { return this.alienScope; }
		@Override
		public TypeBinding checkResolveUsingBaseImportScope(Scope scope, boolean tolerate) {
			return super.checkResolveUsingBaseImportScope(this.alienScope, tolerate);
		}
		@Override
		public TypeBinding resolveType(ClassScope scope) {
			TypeDeclaration referenceContext = scope.referenceContext;
			CheckPoint cp = null;
			if (referenceContext != null)
				cp = referenceContext.compilationResult().getCheckPoint(referenceContext);
			TypeBinding result= super.resolveType(scope);
			if (result != null && result.isValidBinding())
				return result;
			// reset:
			this.resolvedType = null;
			if (cp != null && referenceContext != null) // 2. check redundant via correlation
				referenceContext.compilationResult.rollBack(cp);
			return super.resolveType(this.alienScope.classScope());
		}
		@Override
		public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
			// this variant for use within callin wrappers:
			return super.resolveType((BlockScope) this.alienScope, checkBounds);
		}
		@Override
		protected void reportDeprecatedPathSyntax(Scope scope) {
			// no-op, simply suppress this warning.
		}
	}
	
	
	class AlienScopeArrayQualifiedTypeReference extends ArrayQualifiedTypeReference implements IAlienScopeTypeReference
	{
		Scope alienScope;
		public AlienScopeArrayQualifiedTypeReference(char[][] sources, long[] poss, int dim, Scope alienScope) {
			super(sources, dim, poss);
			this.alienScope = alienScope;
			this.bits |= ASTNode.IsGenerated; // allow qualified reference to role
		}
		public Scope getAlienScope() { return this.alienScope; }
		@Override
		public TypeBinding checkResolveUsingBaseImportScope(Scope scope, boolean tolerate) {
			return super.checkResolveUsingBaseImportScope(this.alienScope, tolerate);
		}
		@Override
		public TypeBinding resolveType(ClassScope scope) {
			TypeDeclaration referenceContext = scope.referenceContext;
			CheckPoint cp = null;
			if (referenceContext != null)
				cp = referenceContext.compilationResult().getCheckPoint(referenceContext);
			TypeBinding result= super.resolveType(scope);
			if (result != null && result.isValidBinding())
				return result;
			// reset:
			this.resolvedType = null;
			if (cp != null && referenceContext != null) // 2. check redundant via correlation
				referenceContext.compilationResult.rollBack(cp);
			return super.resolveType(this.alienScope.classScope());
		}
		@Override
		public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
			// this variant for use within callin wrappers:
			return super.resolveType((BlockScope) this.alienScope, checkBounds);
		}
		@Override
		protected void reportDeprecatedPathSyntax(Scope scope) {
			// no-op, simply suppress this warning.
		}
	}
