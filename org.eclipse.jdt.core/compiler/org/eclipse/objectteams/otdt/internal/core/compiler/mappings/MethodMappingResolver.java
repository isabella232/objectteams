/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2003, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: MethodMappingResolver.java 23416 2010-02-03 19:59:31Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.mappings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.AbstractMethodMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CallinMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.ast.CalloutMappingDeclaration;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;


/**
 * This class implements the second-but-last OT-specific translation:
 * Prepare for transforming callout/in mappings, by resolving method specs,
 * type-checking etc.
 *
 * @author macwitte/haebor
 * @version $Id: MethodMappingResolver.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class MethodMappingResolver
{

	private RoleModel  _role;
	private ClassScope _roleScope;
	/** Index is the methods name+signature */
	private Map<String, List<CalloutMappingDeclaration>> _foundRoleMethods
							= new HashMap<String, List<CalloutMappingDeclaration>>();
	boolean resolveBaseMethods;

	/**
	 * @param role
	 */
	public MethodMappingResolver(RoleModel role, boolean resolveBaseMethods)
	{
		this._role      = role;
		this._roleScope = role.getAst().scope; // we definitely have an AST here
		this.resolveBaseMethods = resolveBaseMethods;
	}

	/**
	 * Main entry for STATE_MAPPINGS_RESOLVED
	 */
	public boolean resolve(boolean doCallout)
	{
		AbstractMethodMappingDeclaration[] methodMappings =
			this._role.getAst().callinCallouts;
		if(methodMappings == null || methodMappings.length == 0)
		{
			// there are no mappings in this role!
			return true;
		}

		boolean result = true;
		for (int idx = 0; idx < methodMappings.length; idx++)
		{
			AbstractMethodMappingDeclaration methodMapping = methodMappings[idx];
			if (methodMapping.isCallout() != doCallout)
				continue;
			if (!this._role.hasBaseclassProblem()) {
				if (this._role.getBinding().baseclass() == null) {
					this._roleScope.problemReporter().methodMappingNotInBoundRole(methodMapping, this._role.getAst());
					methodMapping.tagAsHavingErrors();
					this.resolveBaseMethods = false;
				} else if (methodMapping.isCallin() && this._role.getBinding().baseclass().isInterface()) {
					this._roleScope.problemReporter().callinBindingToInterface(methodMapping, this._role.getBinding().baseclass());
					methodMapping.tagAsHavingErrors();
					this.resolveBaseMethods = false;					
				}
			}

			methodMapping.resolveAnnotations();

			if(methodMapping.isCallout())
			{
				if (this._role._playedByEnclosing) {
					this._roleScope.problemReporter().calloutToEnclosing((CalloutMappingDeclaration)methodMapping, this._role);
					result = false;
				} else {
					result &= resolveCalloutMapping((CalloutMappingDeclaration) methodMapping);
				}
			}
			else // callin:
			{
				result &= resolveCallinMapping((CallinMappingDeclaration) methodMapping);
			}

		}
		if (doCallout) {
			// check for double callout mappings
			for (Iterator<String> iter = this._foundRoleMethods.keySet().iterator(); iter.hasNext();)
			{
				String roleMethodKey = iter.next();
				result &= checkForDuplicateMethodMappings(roleMethodKey);
			}
		}
		
		return result;
	}

	/**
     * Resolve everything about a callin binding except for argument mappings if present.
     * Reports as many errors as can be found.
     * @return true if no error occurred
     */
    private boolean resolveCallinMapping(CallinMappingDeclaration callinMappingDeclaration)
    {
		// main resolving task:
		callinMappingDeclaration.resolveMethodSpecs(this._role, this._role.getBaseTypeBinding(), this.resolveBaseMethods);

		callinMappingDeclaration.binding._roleMethodBinding = callinMappingDeclaration.getRoleMethod();

		return callinMappingDeclaration.getRoleMethod() != null;
    }

	/**
     * Resolve everything about a callin binding except for argument mappings if present.
     * Reports as many errors as can be found.
     * @return true if no error occurred
     */
    private boolean resolveCalloutMapping(CalloutMappingDeclaration calloutMappingDeclaration)
	{
		if (calloutMappingDeclaration.scope == null) { // severe error
			assert calloutMappingDeclaration.hasErrors();
			return false;
		}

		// main resolving task:
		// A callout-with-signatures should always resolve its base method,
		// because that base method could determine the static flag.
		calloutMappingDeclaration.resolveMethodSpecs(this._role,this._role.getBaseTypeBinding(),
													 this.resolveBaseMethods||calloutMappingDeclaration.hasSignature);

//		This binding is part of the interface part of a role:
		MethodBinding roleMethodBinding = calloutMappingDeclaration.roleMethodSpec.resolvedMethod;

		calloutMappingDeclaration.binding._roleMethodBinding = roleMethodBinding;
		if (this.resolveBaseMethods) {
			if (   calloutMappingDeclaration.baseMethodSpec != null
				&& calloutMappingDeclaration.baseMethodSpec.resolvedMethod != null)
			{
				calloutMappingDeclaration.binding._baseMethods = new MethodBinding[]{calloutMappingDeclaration.baseMethodSpec.resolvedMethod};
			} else {
				calloutMappingDeclaration.binding._baseMethods = Binding.NO_METHODS;
				calloutMappingDeclaration.tagAsHavingErrors();
			}
		}
		if (   roleMethodBinding != null 
		    && (roleMethodBinding.isValidBinding()
			   || (roleMethodBinding.problemId() == ProblemReasons.NotFound && calloutMappingDeclaration.hasSignature))) // short-hand, method will be generated
		{
			// store the methodMapping in a map indexed by the role method's name&signature 
			// for later duplication check.
			String methodKey = String.valueOf(CharOperation.concat(roleMethodBinding.selector, roleMethodBinding.signature()));
			List<CalloutMappingDeclaration> mappings = this._foundRoleMethods.get(methodKey);
			if (mappings == null)
			{
				mappings = new LinkedList<CalloutMappingDeclaration>();
				this._foundRoleMethods.put(methodKey, mappings);
			}
			mappings.add(calloutMappingDeclaration);
		}
		return !calloutMappingDeclaration.hasErrors();
	}

	/**
	 * Report errors if there are more than one mapping for the given
	 * role-method.
	 * @param roleMethodKey name&signature of method to check
	 * @return true if there's only one mapping for the method else false.
	 */
	private boolean checkForDuplicateMethodMappings(String roleMethodKey)
	{
		List<CalloutMappingDeclaration> methodSpecs = this._foundRoleMethods.get(roleMethodKey);
		if (methodSpecs.size() > 1)
		{
			for (Iterator<CalloutMappingDeclaration> iter = methodSpecs.iterator(); iter.hasNext();)
			{
				CalloutMappingDeclaration mapping = iter.next();
				if (mapping.binding._declaringRoleClass == this._role.getBinding())
				{
					mapping.scope.problemReporter().duplicateCalloutBinding(
						this._role.getAst(), mapping.roleMethodSpec);
				}
			}
			return false;
		}
		return true; // no dupes found
	}

}
