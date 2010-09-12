/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2005, 2006 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: PrecedenceBinding.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.lookup;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

/**
 * NEW for OTDT.
 *
 * Resolved version of PrecedenceDeclaration.
 *
 * Responsibilities:
 * + Detect overlap regarding referenced base methods
 *   Two variants called from:
 *   - TypeDeclaration.resolve()              AST
 *   - CallinMethodMapppingsAttribute.merge() Binary
 * + Flatten class based precedence (method callins())
 *
 * @author stephan
 * @version $Id: PrecedenceBinding.java 23417 2010-02-03 20:13:55Z stephan $
 */
public class PrecedenceBinding extends Binding {

	public static final PrecedenceBinding[] NoPrecedences = new PrecedenceBinding[0];
	public ReferenceBinding enclosingType;

	/** Elements are either CallinCalloutBinding (of type CALLIN) or ReferenceBinding or <code>null</code> (unresolvable). */
	private Binding[] elements;

	public PrecedenceBinding(ReferenceBinding enclosingType, NameReference[] callinNames) {
		this.enclosingType = enclosingType;
		this.elements = new Binding[callinNames.length];
		for (int i = 0; i < callinNames.length; i++) {
			this.elements[i] = callinNames[i].binding;
		}
	}

	/** Constructor for binding merged by C3: */
	public  PrecedenceBinding(LinkedList bindingNames) {
		int len = bindingNames.size();
		this.elements = new Binding[len];
		int i=0;
		for (Iterator iter = bindingNames.iterator(); iter.hasNext();) {
			this.elements[i++] = (Binding)iter.next();
		}
	}

	/** Constructor for reconstructing from byte code. */
	public PrecedenceBinding(ReferenceBinding enclosingType, CallinCalloutBinding[] mappings)
	{
		this.enclosingType = enclosingType;
		this.elements = mappings;
	}

	public int kind() {
		return PRECEDENCE;
	}

	/**
	 * Do callins of this and other refer to at least one common base method?
	 * TODO(SH): might need to postpone merging after flattening
	 *           (currently class based precedences are not merged with other lists).
	 *
	 * @param other
	 * @return the answer
	 */
	public boolean hasCommonBaseMethod(PrecedenceBinding other) {
		HashSet<MethodBinding> otherBaseMethods = new HashSet<MethodBinding>();
		for (int i = 0; i < other.elements.length; i++) {
			if (other.elements[i] == null || !other.elements[i].isValidBinding())
				continue;
			if (other.elements[i].kind() == BINDING)
			{
				CallinCalloutBinding otherMapping = (CallinCalloutBinding)other.elements[i];
				MethodBinding[] baseMethods = otherMapping._baseMethods;
				for (int j = 0; j < baseMethods.length; j++) {
					otherBaseMethods.add(baseMethods[j]);
				}
			}
		}
		for (int i = 0; i < other.elements.length; i++) {
			if (this.elements[i] == null || !this.elements[i].isValidBinding())
				continue;
			if (this.elements[i].kind() == BINDING)
			{
				CallinCalloutBinding mapping = (CallinCalloutBinding)this.elements[i];
				MethodBinding[] baseMethods = mapping._baseMethods;
				for (int j = 0; j < baseMethods.length; j++) {
					if (otherBaseMethods.contains(baseMethods[j]))
						return true;
				}
			}
		}
		return false;
	}

	private static boolean hasCommonBaseMethod(CallinCalloutBinding b1, CallinCalloutBinding b2, Scope scope)
	{
		// we really have to iterate because checking method identity would not suffice,
		// overriding methods have to be treated as common, too.
		for (MethodBinding m1 : b1._baseMethods)
			for (MethodBinding m2 : b2._baseMethods) {
				// is it the same method?
				if (m1 != m2) { // easiest check first
					if (m1.isStatic() || m2.isStatic())
						continue; // different and not overriding.
					if (!CharOperation.equals(m1.selector, m2.selector))
						continue;
					if (!CharOperation.equals(m1.signature(), m2.signature()))
						continue;
					// only if declaring classes are related we need further investigation:
					if (!(   m1.declaringClass.isCompatibleWith(m2.declaringClass)
						  || m2.declaringClass.isCompatibleWith(m1.declaringClass)))
						continue;
				}
				if (!isDiscriminatedByClassBindings(m1, m2, b1, b2, scope))
					return true;
			}

		return false;
	}

	/** Do playedBy declarations discriminate two base methods such that no precedence is needed? */
	private static boolean isDiscriminatedByClassBindings(
									MethodBinding bm1,
									MethodBinding bm2,
									CallinCalloutBinding ci1,
									CallinCalloutBinding ci2,
									Scope scope)
	{
		ReferenceBinding role1 = ci1._declaringRoleClass;
		ReferenceBinding role2 = ci2._declaringRoleClass;
		if (role1 == role2)
			return false; // same role = cannot discriminate

		ReferenceBinding base1 = role1.baseclass();
		ReferenceBinding base2 = role2.baseclass();
		if (base1 == null || base2 == null)
			return false; // this is insane, but let's be careful ;-)

		if (bm1.isStatic() && base1 != base2)
			return true; // static methods are ordered by base inheritance.

		// incommensurable base classes after playedBy?
		if (!base1.isCompatibleWith(base2) && !base2.isCompatibleWith(base1))
			return true; // no base object can be lifted to both roles

		return false;
	}

	/**
	 * Do roles of the given team contain callin bindings that overlap in their
	 * base methods and lack regulation by a precedence declaration?
	 *
	 * Call this after all callins and precedence have been resolved (and merged for precedences)
	 * @param teamDecl
	 */
	public static void checkDuplicates(TypeDeclaration teamDecl) {
		CallinCalloutBinding[] callinCallouts = teamDecl.binding.allCallins();
		if (callinCallouts.length < 2)
			return; // not enough elements to contain duplicates ;-)
		for (int i = 0; i < callinCallouts.length; i++) {
			if (callinCallouts[i].type != CallinCalloutBinding.CALLIN)
				continue;
			for (int j = i+1; j < callinCallouts.length; j++) {
				if (callinCallouts[j].type == CallinCalloutBinding.CALLIN)
					if (isPrecedenceMissing(teamDecl.binding.precedences,
											callinCallouts[i],
											callinCallouts[j],
											teamDecl.scope))
					{
						teamDecl.scope.problemReporter()
								.unknownPrecedence(teamDecl, callinCallouts[i], callinCallouts[j]);
					}
			}
		}
	}

	/**
	 * Check whether two callin mappings lack a precedence declaration, ie.:
	 * <ul>
	 * <li> they have the same callin modifier
	 * <li> they have a common base method and
	 * <li> they are not ordered by a precedence declaration.
	 * </ul>
	 */
	public static boolean isPrecedenceMissing(
						PrecedenceBinding[]  precedences,
						CallinCalloutBinding callin1,
						CallinCalloutBinding callin2,
						Scope                scope)
	{
		if (   callin1.callinModifier == callin2.callinModifier
			&& hasCommonBaseMethod(callin1, callin2, scope))
		{
			if (precedences != null) {
				for (int k = 0; k < precedences.length; k++) {
					if (precedences[k].containsBoth(callin1, callin2))
						return false;
				}
			}
			if (CharOperation.equals(callin1.name, callin2.name)) {
				ReferenceBinding role1 = callin1._declaringRoleClass;
				ReferenceBinding role2 = callin2._declaringRoleClass;
				if (role1.isCompatibleWith(role2) || role2.isCompatibleWith(role1))
					return false; // callin overriding
			}
			return true;
		}
		return false;
	}

	/**
     * flatten class-based precedence and return array of callins.
	 * @param eliminateOverrides should overriding bindings be eliminated?
	 * 		These are not regulated by precedence, since only one may apply at a time.
	 * @return non-null array
	 */
	public CallinCalloutBinding[] callins(boolean shallEliminateOverrides) {
		if (this.elements instanceof CallinCalloutBinding[])
			return (CallinCalloutBinding[])this.elements; //already flattened

		LinkedList<CallinCalloutBinding> callins = new LinkedList<CallinCalloutBinding>();

		boolean hasChangedLength = false;
		for (int i = 0; i < this.elements.length; i++) {
			if (this.elements[i] == null)
				continue;
			if (this.elements[i].kind() == BINDING)
			{
				callins.add((CallinCalloutBinding)this.elements[i]);
				continue;
			}
			hasChangedLength = true;
			addCallins((ReferenceBinding)this.elements[i], callins);
		}
		if (shallEliminateOverrides)
			hasChangedLength |= eliminateOverrides(callins);
		CallinCalloutBinding[] result = new CallinCalloutBinding[callins.size()];
		if (hasChangedLength)
			callins.toArray(result);
		else
			System.arraycopy(this.elements, 0, result, 0, result.length);
		this.elements = result;
		return result;
	}

	private boolean eliminateOverrides(LinkedList<CallinCalloutBinding> callins) {
		boolean hasChanged = false;
		for(int i=1; i<callins.size(); i++) {
			CallinCalloutBinding callin_i = callins.get(i);
			for(int j=0; j<i; ) {
				CallinCalloutBinding callin_j = callins.get(j);
				if (CharOperation.equals(callin_j.name, callin_i.name))
				{
					if (callin_j._declaringRoleClass.isCompatibleWith(callin_i._declaringRoleClass))
					{
						hasChanged = true;
						callins.remove(i);
						i--; // stay at current index
						break;
					} else
					if (callin_i._declaringRoleClass.isCompatibleWith(callin_j._declaringRoleClass))
					{
						hasChanged = true;
						callins.remove(j);
						break;
					}
				}
				j++;
			}
		}
		return hasChanged;
	}

	/**
	 * Does this precedence declaration regulate the precedence of the given two callins?
	 */
	private boolean containsBoth(CallinCalloutBinding callin1, CallinCalloutBinding callin2) {
		int numFound = 0;
		for (int i = 0; i < this.elements.length; i++) {
			Binding element = this.elements[i];
			if (element == null) // failed to resolve
				continue;
			if (element.kind() == BINDING) {
				if (sameOrOverridingBinding((CallinCalloutBinding)element, callin1))
					numFound++;
				if (sameOrOverridingBinding((CallinCalloutBinding)element, callin2))
					numFound++;
			} else {
				ReferenceBinding roleType = (ReferenceBinding)element;
				roleType = roleType.roleModel.getClassPartBinding();
				if (roleType == callin1._declaringRoleClass)
					numFound++;
				if (roleType == callin2._declaringRoleClass)
					numFound++;
			}

			if (numFound == 2)
				return true;
		}
		return false;
	}

	private boolean sameOrOverridingBinding(CallinCalloutBinding callin1, CallinCalloutBinding callin2)
	{
		if (callin1 == callin2) return true;
		if (!CharOperation.equals(callin1.name, callin2.name)) return false;
		return (   callin1._declaringRoleClass.isCompatibleWith(callin2._declaringRoleClass)
				|| callin2._declaringRoleClass.isCompatibleWith(callin1._declaringRoleClass));
	}

	/**
	 * Add all callin bindings from 'type' to 'result'
	 * @param type
	 * @param result
	 */
	private void addCallins(ReferenceBinding type, LinkedList<CallinCalloutBinding> result) {
		type = type.roleModel.getClassPartBinding();
		if (type == null)
			return;
		HashSet<CallinCalloutBinding> added = new HashSet<CallinCalloutBinding>();
		CallinCalloutBinding[] callins;

		// first add callins ordered by precedence:
		if (type.precedences != null) {
			for (int i = 0; i < type.precedences.length; i++) {
				PrecedenceBinding precedenceBinding = type.precedences[i];
				callins = precedenceBinding.callins(false); // recursive flattening
				for (int j = 0; j < callins.length; j++) {
					if (!added.contains(callins[j])) {
						added.add(callins[j]);  // for avoiding duplicates
						result.add(callins[j]); // order is preserved here
					}
  				}
			}
		}
		// add remaining callins:
		callins = type.callinCallouts;
		if (callins != null) {
			for (int i = 0; i < callins.length; i++) {
				if (callins[i].type == CallinCalloutBinding.CALLIN) {
					if (!added.contains(callins[i])) {
						added.add(callins[i]);  // for avoiding duplicates
						result.add(callins[i]); // order is preserved here
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#readableName()
	 */
	public char[] readableName() {
		char[][] names = new char[this.elements.length][];
		for (int i = 0; i < this.elements.length; i++) {
			if (this.elements[i] == null)
				names[i] = "<unresolved>".toCharArray(); //$NON-NLS-1$
			else if (this.elements[i].kind() == Binding.BINDING)
				names[i] = ((CallinCalloutBinding)this.elements[i]).name;
			else 
				names[i] = ((ReferenceBinding)this.elements[i]).sourceName();
		}
		return CharOperation.concatWith(names, ',');
	}

	public String toString() {
		return new String(readableName());
	}
}
