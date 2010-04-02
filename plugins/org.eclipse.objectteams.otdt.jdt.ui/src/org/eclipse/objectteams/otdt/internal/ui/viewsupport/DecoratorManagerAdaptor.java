/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: DecoratorManagerAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.viewsupport;

import java.util.HashSet;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

import base org.eclipse.ui.internal.decorators.LightweightDecoratorManager;
import base org.eclipse.ui.internal.decorators.LightweightDecoratorDefinition;

/**
 * This class tweaks the decorator manager in order to apply CUD-decorators
 * also for package fragements. 
 * This is needed to place a team icon onto a team package.
 *  
 * @author stephan
 */
@SuppressWarnings({"restriction", "decapsulation" })
public team class DecoratorManagerAdaptor 
{
	/**
	 * Adapt a decoration manager to allow decorating team packages as types.
	 */
	protected class DecorationManager playedBy LightweightDecoratorManager 
	{
		getDecoratorsFor <- replace getDecoratorsFor;
		@SuppressWarnings("basecall")
		callin LightweightDecoratorDefinition[] getDecoratorsFor(Object element)
		{
			LightweightDecoratorDefinition[] result = base.getDecoratorsFor(element);
			
			if (!(element instanceof IPackageFragment))
				return result;
			
			try {
				ICompilationUnit teamUnit = TeamPackageUtil.getTeamUnit((IPackageFragment)element);
				if (teamUnit == null)
					return result;
				
				LightweightDecoratorDefinition[] additional = base.getDecoratorsFor(teamUnit);
				
				if (additional.length > 0) {
					if (result.length == 0)
						return additional;
					// merge results (applying virtual identity between roles and bases):
					HashSet<LightweightDecoratorDefinition> merged = new HashSet<LightweightDecoratorDefinition>();
					for (LightweightDecoratorDefinition elem : result)
						merged.add(elem);
					for (LightweightDecoratorDefinition elem : additional)
						merged.add(elem);
					return merged.toArray(new LightweightDecoratorDefinition[merged.size()]);
				}					
			} catch (JavaModelException e) {
				return result;
			}
			return result;
		}
	}
	
	/** This role serves only as a handle to an otherwise invisible type. */
	protected class LightweightDecoratorDefinition playedBy LightweightDecoratorDefinition { /* no body */ }
}