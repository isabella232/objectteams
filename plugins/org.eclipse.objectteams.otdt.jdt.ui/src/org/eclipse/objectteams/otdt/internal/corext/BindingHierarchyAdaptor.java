/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BindingHierarchyAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.corext;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import base org.eclipse.jdt.internal.corext.dom.Bindings;
/**
 * This team adapts the override markers in order to filter out the generated
 * relation between a class part and its interface part.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public team class BindingHierarchyAdaptor 
{
	protected class OverrideMarkerAdaptor playedBy Bindings
	{
		findOverriddenMethodInHierarchy <- replace findOverriddenMethodInHierarchy;
		@SuppressWarnings("basecall")
		static callin IMethodBinding findOverriddenMethodInHierarchy(ITypeBinding type, IMethodBinding binding)
		{
			ITypeBinding declaringClass = binding.getDeclaringClass();
			if (declaringClass.isClassPartOf(type)) {
				// if type is the interfacepart of declaringClass
				// the real superInterfaces are in type.getInterfaces():
				for (ITypeBinding superIfc : type.getInterfaces()) {
					IMethodBinding method = base.findOverriddenMethodInHierarchy(superIfc, binding);
					if (method != null)
						return method;
				}
				return null; 
			}
			return base.findOverriddenMethodInHierarchy(type, binding);
		}
	}
}
