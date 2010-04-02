/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2004, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: PrecedenceDeclaration.java 23401 2010-02-02 23:56:05Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.ast;

import java.util.LinkedList;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.objectteams.otdt.core.exceptions.InternalCompilerError;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.CallinCalloutBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.PrecedenceBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.TeamModel;


/**
 * NEW for OTDT:
 * Represents the 'precedence bindingName,..;' statement.
 *
 * Life cycle:
 * + created by Parser.consumePrecedenceDeclaration()
 * + invoked from TypeDeclaration.resolve():
 *   - before resolving member types: resolve()
 *   - after resolving member types: merge precedence lists
 *
 * Further processing:
 * 	  creation of CallinPrecedenceAttribute
 *    semantic checking is done by PrecedenceBinding
 *
 * @author stephan
 * @version $Id: PrecedenceDeclaration.java 23401 2010-02-02 23:56:05Z stephan $
 */
public class PrecedenceDeclaration extends ASTNode {

	public NameReference[] bindingNames;
	public int declarationSourceStart;
	public PrecedenceBinding binding;

	public PrecedenceDeclaration(int start, int end, NameReference[] bindingNames) {
		this.bindingNames = bindingNames;
		this.sourceStart = bindingNames[0].sourceStart;
		this.sourceEnd   = end;
		this.declarationSourceStart = start;
	}

	/**
	 * Resolve the name references in this precedence declaration.
	 * Push resolved declarations out to the enclosing team, if applicable.
	 *
	 * @param type enclosing type
	 */
	public PrecedenceBinding resolve(TypeDeclaration type) {

		names: for (int i = 0; i < this.bindingNames.length; i++) {
			NameReference name = this.bindingNames[i];
			ReferenceBinding enclosing = type.binding;
			char[] token;
			if (name instanceof SingleNameReference) {
				SingleNameReference sref = (SingleNameReference)name;
				token = sref.token;
				sref.binding = findCallinInType(type.scope, type.binding, sref.token);
			} else {
				assert name instanceof QualifiedNameReference;
				QualifiedNameReference qref = (QualifiedNameReference)name;
				for (int j = 0; j < qref.tokens.length-1; j++) {
					char [] roleName = qref.tokens[j];
					enclosing = type.scope.getMemberType(roleName, enclosing);
					if (!enclosing.isValidBinding()) {
						type.scope.problemReporter().invalidType(name, enclosing);
						continue names;
					}
				}
				token = qref.tokens[qref.tokens.length-1];
				qref.binding = findCallinInType(type.scope, enclosing, token);
			}
			if (name.binding == null) {
				type.scope.problemReporter().callinBindingNotFound(name, token, enclosing);
				char[] missingName = CharOperation.concat("missingCallin:".toCharArray(), token); //$NON-NLS-1$
				name.binding = new CallinCalloutBinding(type.binding, missingName);
			}
		}
		checkOverriding(this.bindingNames, type.scope);
		this.binding = new PrecedenceBinding(type.binding, this.bindingNames);
		if (   type.enclosingType != null
			&& type.enclosingType.isTeam())
		{
			type.enclosingType.addResolvedPrecedenceDeclaration(
					type.binding.sourceName(),
					this);
		}
		return this.binding;
	}

	/**
	 * Does list of bindings contain a pair of callins, of which one overrides the other?
	 * Signal a problem if so.
	 *
	 * @param bindingNames
	 */
	private static void checkOverriding(NameReference[] bindingNames, Scope scope)
	{
		for (int i = 1; i < bindingNames.length; i++) {
			NameReference ref_i = bindingNames[i];
			if (   ref_i.binding != null
				&& ref_i.binding.kind() == Binding.BINDING)
			{
				for (int j = 0; j < i; j++) {
					NameReference ref_j = bindingNames[j];
					if (   ref_j.binding != null
						&& ref_j.binding.kind() == Binding.BINDING)
					{
						CallinCalloutBinding callin_i = (CallinCalloutBinding)ref_i.binding;
						CallinCalloutBinding callin_j = (CallinCalloutBinding)ref_j.binding;
						if (!CharOperation.equals(callin_i.name, callin_j.name))
							continue;
						ReferenceBinding role_i = callin_i._declaringRoleClass;
						ReferenceBinding role_j = callin_j._declaringRoleClass;
						if (role_i.isCompatibleWith(role_j))
							scope.problemReporter().precedenceForOverriding(ref_i, ref_j);
						if (role_j.isCompatibleWith(role_i))
							scope.problemReporter().precedenceForOverriding(ref_j, ref_i);
					}
				}
			}
		}
	}

	/**
	 * Resolve an element of a precedence declaration.
	 *
	 * @param scope
	 * @param type
	 * @param name
	 * @return either CallinCalloutBinding or ReferenceBinding or null
	 */
	private Binding findCallinInType(Scope scope,
									 ReferenceBinding type,
									 char[] name)
	{
		if (type.isRole()) {
			Binding found = findCallinInRole(type, name);
			if (found != null)
				return found;
		}
		if (type.isTeam()) {
			ReferenceBinding roleBinding = type.getMemberType(name);
			if (roleBinding != null)
				return roleBinding;
			return null;
		}
		if (type.isRole())
			return null; // tried before, no success.
		scope.problemReporter().illegalEnclosingForCallinName(this, type, name);
		return null;
	}

	/** Same as above, but now we now that type is a role. */
	private static CallinCalloutBinding findCallinInRole(ReferenceBinding type, char[] name)
	{
		assert type.isRole();
		RoleModel roleModel = type.roleModel;
		type = roleModel.getClassPartBinding();
		ReferenceBinding current = type;
		while (current != null && current.isRole())
		{
			if (current.callinCallouts != null) {
				for (int i = 0; i < current.callinCallouts.length; i++) {
					CallinCalloutBinding mapping = current.callinCallouts[i];
					if (mapping.type == CallinCalloutBinding.CALLIN)
					{
						if (CharOperation.equals(name, mapping.name))
							return mapping;
					}
				}
			}
			current = current.superclass();
		}
		ReferenceBinding[] tsupers = roleModel.getTSuperRoleBindings();
		for (int i = 0; i < tsupers.length; i++) {
			CallinCalloutBinding callinBinding = findCallinInRole(tsupers[i], name);
			if (callinBinding != null) {
				// create an unresolved binding:
				// (details to be filled by by CallinMethodMappingsAttribute.merge->createBinding())
				CallinCalloutBinding result = new CallinCalloutBinding(type, name);
				type.addCallinCallouts(new CallinCalloutBinding[]{result});
				return result;
			}
		}
		return null;
	}

	/**
	 * Merge all precedence lists referring to the same base method.
	 * Uses the C3-algorithm for linearization.
	 *
	 * @param type enclosing type, member types have been resolved
	 * @return the (possibly reduced) array of merged lists.
	 */
	public static PrecedenceBinding[] mergePrecedences(TypeDeclaration type)
	{
		int len = (type.precedences != null) ? type.precedences.length : 0;
		// TODO (SH): tsuper teams!
		ReferenceBinding superTeam = type.binding.superclass();
		int lenSuper = (superTeam != null) ? superTeam.precedences.length : 0;
		if (len+lenSuper == 0)
			return PrecedenceBinding.NoPrecedences;

		PrecedenceBinding[] precedences = new PrecedenceBinding[len + lenSuper];
		for (int i = 0; i < len; i++) {
			precedences[i] = type.precedences[i].binding;
		}
		for (int i = 0; i < lenSuper; i++) {
			precedences[len+i] = strengthenPrecendence(type, superTeam.precedences[i]);
		}
		int count = precedences.length;
		for(int i=0; i<precedences.length-1; i++) {
			if (precedences[i] == null)
				continue;
			for (int j = i+1; j < precedences.length; j++) {
				if (precedences[j] == null)
					continue;
				if (!precedences[i].hasCommonBaseMethod(precedences[j]))
					continue;

				LinkedList merged = new LinkedList();
				CallinCalloutBinding[] p1 = precedences[i].callins(false);
				CallinCalloutBinding[] p2 = precedences[j].callins(false);
				if (c3Merge(
						p1, p1.length-1,
						p2, p2.length-1,
						merged))
				{
					precedences[i] = new PrecedenceBinding(merged);
					precedences[j] = null;
					count--;
				} else {
					if (i < len)
						type.scope.problemReporter().incompatiblePrecedenceLists(type.precedences[i].bindingNames[i], type);
					else
						throw new InternalCompilerError("Incompatible inherited precedence lists"); //$NON-NLS-1$
				}
			}
		}
		if (count < precedences.length) {
			PrecedenceBinding[] newPrecs = new PrecedenceBinding[count];
			int j = 0;
			for (int i = 0; i < precedences.length; i++) {
				if (precedences[i] != null)
					newPrecs[j++] = precedences[i];
			}
			precedences = newPrecs;
		}
		return precedences;
	}

	/**
	 * Update all callin bindings from super team to current team
	 */
	private static PrecedenceBinding strengthenPrecendence(TypeDeclaration site,
														   PrecedenceBinding precedenceBinding)
	{
		CallinCalloutBinding[] superCallins = precedenceBinding.callins(false);
		CallinCalloutBinding[] callins = new CallinCalloutBinding[superCallins.length];
		for (int i = 0; i < callins.length; i++) {
			ReferenceBinding roleType =	(ReferenceBinding)TeamModel
					.strengthenRoleType(site.binding, superCallins[i]._declaringRoleClass);
			callins[i] = findCallinInRole(roleType, superCallins[i].name);
		}
		return new PrecedenceBinding(site.binding, callins);
	}

	// The algorithm from literature:
	private static boolean c3Merge(
			CallinCalloutBinding[] p1, int i1,
			CallinCalloutBinding[] p2, int i2,
			LinkedList result)
	{
		if (i1 < 0 && i2 < 0) // both lists empty?
			return true;
		if (   i2 >= 0										   // have a bm?
			&& !containsCallinBinding(p2[i2], p1, i1)) // bm in {a1..an}?
		{
			result.addFirst(p2[i2]);
			return c3Merge(p1, i1, p2, i2-1, result);
		} else
		if (   i1 >= 0										   // have an an?
			&& !containsCallinBinding(p1[i1], p2, i2)) // an in {b1..bm}?
		{
			result.addFirst(p1[i1]);
			return c3Merge(p1, i1-1, p2, i2, result);
		} else
	    if (p1[i1] == p2[i2]) {
	    	result.addFirst(p1[i1]);
	    	return c3Merge(p1, i1-1, p2, i2-1, result);
	    } else {
	    	return false;
	    }
	}

	private static boolean containsCallinBinding(Binding 			    binding,
												 CallinCalloutBinding[] callins,
												 int                    last)
	{
		for (int i = last; i >= 0; i--) {
			if (callins[i] == binding)
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.ASTNode#print(int, java.lang.StringBuffer)
	 */
	public StringBuffer print(int indent, StringBuffer output) {
		printIndent(indent, output);
		output.append("precedence "); //$NON-NLS-1$
		for (int i = 0; i < this.bindingNames.length; i++) {
			this.bindingNames[i].print(indent, output);
			output.append(i == this.bindingNames.length-1 ? ';' : ',');
		}
		return output;
	}

}
