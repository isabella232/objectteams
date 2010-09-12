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
 * $Id: TraverseRequestor.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core.hierarchy;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;

/**
 * @author svacina
 */
abstract public class TraverseRequestor
{
	

	static class HierarchyContext {
		boolean isFocusType;
		boolean isExplicitSuperclass;
		boolean isBehindExplicitInheritance;
		public HierarchyContext(boolean isFocusType, boolean isExplicitSuperclass, boolean isBehindExplicitInheritance) {
			super();
			this.isFocusType = isFocusType;
			this.isExplicitSuperclass = isExplicitSuperclass;
			this.isBehindExplicitInheritance = isBehindExplicitInheritance;
		}
	}
	
	protected IType _focusType = null;
	
	
	public void report(IType type, HierarchyContext context){}
	
	public void report(IMethod method, HierarchyContext context){}
	
	public void report(ICallinMapping callinMapping, HierarchyContext context){}
	
	public void report(ICalloutMapping calloutMapping, HierarchyContext context){}
	
	public void report(ICalloutToFieldMapping calloutToFieldMapping, HierarchyContext context){}

	public void report(IField field, HierarchyContext context){}
	
	public IType getFocusType()
	{
		return _focusType;		
	}
}
