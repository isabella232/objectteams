/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.TypeAnchorReference;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ProblemAnchorBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.FieldModel;

/**
 * OTDT changes:
 * What: support base bound
 */
public class TypeParameter extends AbstractVariableDeclaration {

    public TypeVariableBinding binding;
	public TypeReference[] bounds;

	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration#getKind()
	 */
	public int getKind() {
		return TYPE_PARAMETER;
	}

//{ObjectTeams: new query
	public boolean hasBaseBound() {
		return this.type != null && (this.type.bits & IsRoleType) != 0;
	}
// SH}

	public void checkBounds(Scope scope) {

		if (this.type != null) {
			this.type.checkBounds(scope);
		}
		if (this.bounds != null) {
			for (int i = 0, length = this.bounds.length; i < length; i++) {
				this.bounds[i].checkBounds(scope);
			}
		}
	}

	private void internalResolve(Scope scope, boolean staticContext) {
	    // detect variable/type name collisions
		if (this.binding != null) {
			Binding existingType = scope.parent.getBinding(this.name, Binding.TYPE, this, false/*do not resolve hidden field*/);
			if (existingType != null
					&& this.binding != existingType
					&& existingType.isValidBinding()
					&& (existingType.kind() != Binding.TYPE_PARAMETER || !staticContext)) {
				scope.problemReporter().typeHiding(this, existingType);
			}
		}
	}

	public void resolve(BlockScope scope) {
		internalResolve(scope, scope.methodScope().isStatic);
	}

	public void resolve(ClassScope scope) {
		internalResolve(scope, scope.enclosingSourceType().isStatic());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.AstNode#print(int, java.lang.StringBuffer)
	 */
	public StringBuffer printStatement(int indent, StringBuffer output) {
		output.append(this.name);
		if (this.type != null) {
//{ObjectTeams: role type bound:
		  if (hasBaseBound())
			output.append(" base "); //$NON-NLS-1$
		  else
// SH}
			output.append(" extends "); //$NON-NLS-1$
			this.type.print(0, output);
		}
		if (this.bounds != null){
			for (int i = 0; i < this.bounds.length; i++) {
				output.append(" & "); //$NON-NLS-1$
				this.bounds[i].print(0, output);
			}
		}
		return output;
	}

	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	    // nothing to do
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.type != null) {
				this.type.traverse(visitor, scope);
			}
			if (this.bounds != null) {
				int boundsLength = this.bounds.length;
				for (int i = 0; i < boundsLength; i++) {
					this.bounds[i].traverse(visitor, scope);
				}
			}
		}
		visitor.endVisit(this, scope);
	}

	public void traverse(ASTVisitor visitor, ClassScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.type != null) {
				this.type.traverse(visitor, scope);
			}
			if (this.bounds != null) {
				int boundsLength = this.bounds.length;
				for (int i = 0; i < boundsLength; i++) {
					this.bounds[i].traverse(visitor, scope);
				}
			}
		}
		visitor.endVisit(this, scope);
	}
//{ObjectTeams: for type parameters with an anchor (C<R<@t..>>)
	public void connectTypeAnchors(Scope scope) {		
		if (this.type instanceof TypeAnchorReference) {
			int numParams = 1;
			if (this.bounds != null)
				numParams += this.bounds.length;
			ITeamAnchor[] anchors = new ITeamAnchor[numParams];
			anchors[0] = ((TypeAnchorReference) this.type).resolveAnchor(scope);
			if (!ProblemAnchorBinding.checkAnchor(scope, this.type, anchors[0], this.name))
				return;
			if (anchors[0] instanceof FieldBinding && anchors[0].isValidBinding())
				FieldModel.getModel((FieldBinding)anchors[0]).addUsageRank(this.binding.rank);
			// FIXME(SH): else store this in the AnchorListAttribute!
			int boundCnt = 0;
			if (this.bounds != null)
				for (int i=0; i<this.bounds.length; i++) {
					if (this.bounds[i] instanceof TypeAnchorReference) {
						anchors[1+i] = ((TypeAnchorReference)this.bounds[i]).resolveAnchor(scope);
					} else {
						// poor man's version of resolving static type bounds:
						TypeBinding bound = this.bounds[i].resolveType((ClassScope)scope);
						if (++boundCnt == 1)
							this.binding.superclass = (ReferenceBinding) bound;
						else
							scope.problemReporter().incompleteDependentTypesImplementation(this, "Cannot combine more than one type bound with an instance bound");
					}						
				}
			this.binding.anchors = anchors;
		}
	}
// SH}
}
