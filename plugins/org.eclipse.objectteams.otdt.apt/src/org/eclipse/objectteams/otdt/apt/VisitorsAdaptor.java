/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2009 Stephan Herrmann
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: VisitorsAdaptor.java 23449 2010-02-04 20:26:45Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 **********************************************************************/
package org.eclipse.objectteams.otdt.apt;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.AptSourceLocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.Dependencies;
import org.eclipse.objectteams.otdt.internal.core.compiler.control.ITranslationStates;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;

import base org.eclipse.jdt.internal.compiler.apt.dispatch.AnnotationDiscoveryVisitor;

/**
 * This class adapts visitors used in annotation processing that need to be aware of
 * OT extensions of the compiler ast - 
 * currently only {@link org.eclipse.jdt.internal.compiler.apt.dispatch.AnnotationDiscoveryVisitor}.
 * 
 * @author stephan
 * @since 1.2.7
 */
@SuppressWarnings("restriction")
public team class VisitorsAdaptor {
	protected class AnnotationDiscoveryVisitor playedBy AnnotationDiscoveryVisitor {

		@SuppressWarnings("decapsulation")
		void resolveAnnotations(BlockScope arg0, Annotation[] arg1, Binding arg2)
			-> void resolveAnnotations(BlockScope arg0, Annotation[] arg1, Binding arg2);


		boolean visit(Argument argument, BlockScope scope) 
			<- replace boolean visit(Argument argument, BlockScope scope);


		@SuppressWarnings("basecall")
		callin boolean visit(Argument argument, BlockScope scope) {
			if (scope.kind == Scope.BINDING_SCOPE) { // don't blindly assume that we're inside a method
				Annotation[] annotations = argument.annotations;
				// the payload (orig uses cast to AbstractMethodDeclaration):
				AbstractMethodMappingDeclaration mapping = (AbstractMethodMappingDeclaration) scope.referenceContext();
				RoleModel roleModel = scope.referenceType().getRoleModel();
				if (roleModel != null)
					Dependencies.ensureRoleState(roleModel, ITranslationStates.STATE_MAPPINGS_RESOLVED); // needed for accessing the method binding.
				MethodBinding binding = mapping.getRoleMethod(); 
				//
				if (binding != null) {
					TypeDeclaration typeDeclaration = scope.referenceType();
					typeDeclaration.binding.resolveTypesFor(binding);
					if (argument.binding != null) {
						argument.binding = new AptSourceLocalVariableBinding(argument.binding, binding);
					}
				}
				if (annotations != null) {
					this.resolveAnnotations(
							scope,
							annotations,
							argument.binding);
				}
				return false;
			} else {
				return base.visit(argument, scope);
			}
		}
	}
}
