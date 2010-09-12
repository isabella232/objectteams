/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: QuickFixCoreAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.assist;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;

import base org.eclipse.jdt.core.dom.MethodBinding;

/**
 * This team adapts dom elements for use by quickfixes.
 * It is activated only temporarily from {@link OTQuickFixes}.
 * 
 * @author stephan
 * @since 1.2.1
 */
@SuppressWarnings("restriction")
public team class QuickFixCoreAdaptor {

	

	/** 
	 * Adapt role creator methods to behave like regular constructors.
	 */	
	@SuppressWarnings("decapsulation")
	protected class MethodBinding playedBy MethodBinding 
	{
		ITypeBinding   rawgetDeclaringClass() -> ITypeBinding getDeclaringClass();
		String         rawgetName()           -> String getName();
		ITypeBinding   getReturnType()        -> ITypeBinding   getReturnType();
		ITypeBinding[] getParameterTypes()    -> ITypeBinding[] getParameterTypes();
		
		boolean isCreator() -> get org.eclipse.jdt.internal.compiler.lookup.MethodBinding binding // avoiding triggering callin on getName()
			with { result <- CharOperation.prefixEquals(IOTConstants.CREATOR_PREFIX_NAME,binding.selector) }
		
		
		IMethodBinding getMethodDeclaration() <- replace IMethodBinding getMethodDeclaration();

		/*
		 * Lookup the method binding from the actual role constructor.
		 */
		@SuppressWarnings({ "basecall", "restriction" })
		callin IMethodBinding getMethodDeclaration() 
		{
			ITypeBinding declaringClass = null;
			String methodName = null;
			if (isCreator()) {
				declaringClass = getRealClass(rawgetDeclaringClass(), getReturnType());
				methodName = getReturnType().getName(); // ctor named as the class
			} else {
				ITypeBinding current = rawgetDeclaringClass();
				if (current.isRole() && current.isInterface()) {
					declaringClass = getRealClass(current.getDeclaringClass(), current);
					methodName = rawgetName();
				}
			}
			if (declaringClass != null) {
				IMethodBinding method = Bindings.findMethodInType(declaringClass, methodName, getParameterTypes());
				if (method != null)
					return method;
			}
			deactivate(); // nothing ot-specific - stop intercepting;
			return base.getMethodDeclaration();
		}

		String getName() <- replace String getName()
			base when (!isExecutingCallin()); // avoid re-entrance
		
		/*
		 * Trim the creator's name to look like a ctor.
		 */
		callin String getName() {
			String baseName = base.getName();
			String CREATOR_PREFIX = new String(IOTConstants.CREATOR_PREFIX_NAME);
			if (baseName.startsWith(CREATOR_PREFIX))
				return baseName.substring(CREATOR_PREFIX.length());
			return baseName;
		}
		
		
		ITypeBinding getDeclaringClass() <- replace ITypeBinding getDeclaringClass()
			base when (!isExecutingCallin());

		/*
		 * Replace the team (declaring class of the creator method) with the role class.
		 */		
		@SuppressWarnings("basecall")
		callin ITypeBinding getDeclaringClass() {
			if (isCreator())
				return getRealClass(rawgetDeclaringClass(), getReturnType());
			return base.getDeclaringClass();
		}

		/*
		 * While normally role ifcs are preferred, for ast rewriting we need the real role class.
		 */
		ITypeBinding getRealClass(ITypeBinding enclosingTeam, ITypeBinding roleIfc) {
			String name = roleIfc.getName();
			for (ITypeBinding member: enclosingTeam.getDeclaredTypes())
				if (member.getName().equals(name) && member.isClass())
					return member;
			return null;
		}		
	}
	
}
