/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: HierarchyUtilAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.util;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.internal.core.OTTypeHierarchy;

import base org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import base org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import base org.eclipse.jdt.internal.ui.util.OpenTypeHierarchyUtil;

/**
 * This team adapts different utility classes relating to hierarchies.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public team class HierarchyUtilAdaptor 
{
	/** Also consider method mappings as candidates for a hierarchy. */ 
	protected class OpenTypeHierarchy playedBy OpenTypeHierarchyUtil 
	{		
		@SuppressWarnings("basecall")
		static callin IJavaElement[] getCandidates(Object input) {
			if (input instanceof IJavaElement) {
				IJavaElement elem= (IJavaElement) input;
				switch (elem.getElementType()) {
					case IOTJavaElement.CALLIN_MAPPING:
					case IOTJavaElement.CALLOUT_MAPPING:
					case IOTJavaElement.CALLOUT_TO_FIELD_MAPPING:
						return new IJavaElement[] { elem };
				} 
			}
			return base.getCandidates(input);
		}
		getCandidates <- replace getCandidates;
	}
	
	/** Consider multiple super classes for roles. */
	@SuppressWarnings("decapsulation"/*final baseclass*/) 
	protected class JavaModelUtil playedBy JavaModelUtil 
	{
		@SuppressWarnings("basecall")
		static callin boolean isSuperType(ITypeHierarchy hierarchy, IType possibleSuperType, IType type)
		{
			if (hierarchy instanceof OTTypeHierarchy) {
		        IOTType otType = OTModelManager.getOTElement(type);
		        if (otType != null && otType.isRole())
				{
					IType[] supertypes = ((OTTypeHierarchy)hierarchy).getAllSupertypes(type);
					for (IType supertype : supertypes) 
						if (supertype.equals(possibleSuperType))
							return true;
					return false;
				}
			}
			return base.isSuperType(hierarchy, possibleSuperType, type);
		}
		isSuperType <- replace isSuperType;
	}
	
	/** Find overridden methods in all kinds of super types. */
	protected class MethodOverrideTester playedBy MethodOverrideTester 
	{
		@SuppressWarnings("basecall")
		callin IMethod findOverriddenMethod(IMethod overriding, boolean testVisibility) 
			throws JavaModelException 
		{
			ITypeHierarchy hierarchy = getHierarchy();
			if (hierarchy instanceof OTTypeHierarchy) 
			{
				IType declaringType = overriding.getDeclaringType();
				IOTType declaringOTType = OTModelManager.getOTElement(declaringType);
				if (declaringOTType != null && declaringOTType.isRole()) 
				{
					int flags= overriding.getFlags();
					if (Flags.isPrivate(flags) || Flags.isStatic(flags) || overriding.isConstructor()) {
						return null;
					}
					
					IType[] supertypes = ((OTTypeHierarchy)hierarchy).getAllSupertypes(declaringType);
					for (IType type : supertypes) {
						IMethod method = findOverriddenMethodInType(type, overriding);
						if (method != null)
							return method;
					}
					return null;
				}
			}
			return base.findOverriddenMethod(overriding, testVisibility);
		}
		findOverriddenMethod <- replace findOverriddenMethod;
		
		abstract IMethod findOverriddenMethodInType(IType overriddenType, IMethod overriding)
			throws JavaModelException;
		
		findOverriddenMethodInType -> findOverriddenMethodInType;
		
		@SuppressWarnings("decapsulation")
		ITypeHierarchy getHierarchy() -> get ITypeHierarchy fHierarchy;
	}
}
