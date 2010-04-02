/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2008 Technical University Berlin, Germany.
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
package org.eclipse.objectteams.otdt.internal.core.compiler.statemachine.copyinheritance;

import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
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
interface IAlienScopeTypeReference {
	ClassScope getAlienScope();
}

	// ===== And now for some implementing classes: =====

	class AlienScopeSingleTypeReference extends SingleTypeReference implements IAlienScopeTypeReference
	{
		ClassScope alienScope;
		public AlienScopeSingleTypeReference(char[] source, long pos, ClassScope alienScope) {
			super(source, pos);
			this.alienScope = alienScope;
		}
		public ClassScope getAlienScope() { return this.alienScope; }
		@Override
		public TypeBinding checkResolveUsingBaseImportScope(Scope scope) {
			return super.checkResolveUsingBaseImportScope(this.alienScope);
		}
		@Override
		public TypeBinding resolveType(ClassScope scope) {
			// `scope` may be stronger then `alienScope`, try it first:
			// (see 1.1.27-otjld-constructor-of-nested-team-2)
			TypeBinding result= super.resolveType(scope);
			if (result != null && result.isValidBinding())
				return result;
			// remove problem binding if any:
			this.resolvedType = null;
			return super.resolveType(this.alienScope);
		}
	}

	// exactly like AlienScopeSingleTypeReference, but different super class
	class AlienScopeParameterizedSingleTypeReference extends ParameterizedSingleTypeReference implements IAlienScopeTypeReference
	{
		ClassScope alienScope;
		public AlienScopeParameterizedSingleTypeReference(char[] source, TypeReference[] typeArguments, int dimensions, long pos, ClassScope alienScope) {
			super(source, typeArguments, dimensions, pos);
			this.alienScope = alienScope;
		}
		public ClassScope getAlienScope() { return this.alienScope; }
		@Override
		public TypeBinding checkResolveUsingBaseImportScope(Scope scope) {
			// `scope` may be stronger then `alienScope`, try it first:
			// (see 1.1.22-otjld-layered-teams-5)
			TypeBinding result= super.checkResolveUsingBaseImportScope(scope);
			if (result != null && result.isValidBinding())
				return result;
			// remove problem binding if any:
			this.resolvedType = null;
			return super.checkResolveUsingBaseImportScope(this.alienScope);
		}
		@Override
		public TypeBinding resolveType(ClassScope scope) {
			// `scope` may be stronger then `alienScope`, try it first:
			// (see 1.1.27-otjld-constructor-of-nested-team-2)
			TypeBinding result= super.resolveType(scope);
			if (result != null && result.isValidBinding())
				return result;
			// remove problem binding if any:
			this.resolvedType = null;
			return super.resolveType(this.alienScope);
		}
	}
	
	class AlienScopeQualifiedTypeReference extends QualifiedTypeReference implements IAlienScopeTypeReference
	{
		ClassScope alienScope;
		public AlienScopeQualifiedTypeReference(char[][] sources, long[] poss, ClassScope alienScope) {
			super(sources, poss);
			this.alienScope = alienScope;
		}
		public ClassScope getAlienScope() { return this.alienScope; }
		@Override
		public TypeBinding checkResolveUsingBaseImportScope(Scope scope) {
			return super.checkResolveUsingBaseImportScope(this.alienScope);
		}
		@Override
		public TypeBinding resolveType(ClassScope scope) {
			TypeBinding result= super.resolveType(scope);
			if (result != null && result.isValidBinding())
				return result;
			// reset:
			this.resolvedType = null;
			return super.resolveType(this.alienScope);
		}
		@Override
		protected void reportDeprecatedPathSyntax(Scope scope) {
			// no-op, simply suppress this warning.
		}
	}
