/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 20038 Technical University Berlin, Germany.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Sorting.java 23417 2010-02-03 20:13:55Z stephan $
 *
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 *
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core.compiler.util;

import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.objectteams.otdt.internal.core.compiler.model.RoleModel;

/**
 * Sorting utilities.
 *
 * @author stephan
 * @since 1.2.1
 */
public class Sorting {

	/**
	 * Topological sort for member types with depth first search.
	 * Guarantee: supertypes come before subtypes.
	 */
	public static void sortMemberTypes(SourceTypeBinding enclosing) {
		int len = enclosing.memberTypes.length;

		ReferenceBinding[] unsorted = enclosing.memberTypes;

		ReferenceBinding[] sorted = new ReferenceBinding[len];
		int o = 0;
		for(int i=0; i<len; i++)
			o = sort(enclosing, unsorted, i, sorted, o);

		enclosing.memberTypes = sorted;
	}
	// Transfer input[i] and all its supers into output[o] ff.
	private static int sort(ReferenceBinding enclosing,
							ReferenceBinding[] input, int i,
							ReferenceBinding[] output, int o)
	{
		if (input[i] == null)
			return o;

		ReferenceBinding superclass = input[i].superclass();
		o = sortSuper(enclosing, superclass, input, output, o);

		for (ReferenceBinding superIfc : input[i].superInterfaces())
			o = sortSuper(enclosing, superIfc, input, output, o);

		// done with supers, now input[i] can safely be transferred:
		output[o++] = input[i];
		input[i] = null;

		return o;
	}
	// if superclass is within the set of member types to sort,
	// transfer it and all its supers to output[o] ff.
	private static int sortSuper(ReferenceBinding enclosing,
						  		 ReferenceBinding superclass,
						  		 ReferenceBinding[] input,
						  		 ReferenceBinding[] output, int o)
	{
		if (   superclass != null // inspecting super of Confined?
			&& superclass.id != TypeIds.T_JavaLangObject
			&& superclass.enclosingType() == enclosing) // is super within scope?
		{
			// search superclass within input:
			int j = 0;
			for(j=0; j<input.length; j++)
				if (input[j] == superclass)
					break;
			if (j < input.length)
				// depth first traversal:
				o = sort(enclosing, input, j, output, o);
			// otherwise assume super was already transferred.
		}
		return o;
	}

	// --- similar for role models:
	
	public static RoleModel[] sortRoles(RoleModel[] unsorted) {
		int len = unsorted.length;
		
		RoleModel[] sorted = new RoleModel[len];
		int o = 0;
		for(int i=0; i<len; i++)
			o = sort(unsorted, i, sorted, o);
		
		return sorted;
	}
	
	// Transfer input[i] and all its supers into output[o] ff.
	private static int sort(RoleModel[] input, int i,
							RoleModel[] output, int o)
	{
		if (input[i] == null || input[i].getBinding() == null)
			return o;
		
		ReferenceBinding inBinding = input[i].getBinding();
		ReferenceBinding superclass = inBinding.superclass();
		o = sortSuper(superclass, input, output, o);

		for (ReferenceBinding superIfc : inBinding.superInterfaces())
			o = sortSuper(superIfc, input, output, o);

		// done with supers, now input[i] can safely be transferred:
		output[o++] = input[i];
		input[i] = null;

		return o;
	}
	// if superclass is within the set of member types to sort,
	// transfer it and all its supers to output[o] ff.
	private static int sortSuper(ReferenceBinding superclass,
						  		 RoleModel[] input,
						  		 RoleModel[] output, int o)
	{
		if (   superclass != null // inspecting super of Confined?
			&& superclass.id != TypeIds.T_JavaLangObject) // is super within scope?
		{
			boolean inScope = false;
			for (RoleModel rm : input)
				if (rm != null && rm.getBinding() == superclass) {
					inScope = true;
					break;
				}
			if (inScope) {
					
				// search superclass within input:
				int j = 0;
				for(j=0; j<input.length; j++)
					if (input[j].getBinding() == superclass)
						break;
				if (j < input.length)
					// depth first traversal:
					o = sort(input, j, output, o);
				// otherwise assume super was already transferred.
			}
		}
		return o;
	}
}
