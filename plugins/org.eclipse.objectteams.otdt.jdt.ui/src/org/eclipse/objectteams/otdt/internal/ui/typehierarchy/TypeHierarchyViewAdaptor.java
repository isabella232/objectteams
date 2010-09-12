/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2006, 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: TypeHierarchyViewAdaptor.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui.typehierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.typehierarchy.TypeHierarchyLifeCycle;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.hierarchy.OTTypeHierarchies;
import org.eclipse.objectteams.otdt.ui.Util;

import base org.eclipse.jdt.internal.ui.typehierarchy.MethodsContentProvider;
import base org.eclipse.jdt.internal.ui.typehierarchy.TraditionalHierarchyViewer.TraditionalHierarchyContentProvider;

/**
 * This team adapts the type hierarchy to show method mappings.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public team class TypeHierarchyViewAdaptor 
{
	private static final Object[] NO_ELEMENTS = new Object[0];
	
	/** Filter generated elements and also provide method mappings. */
	protected class MethodsContentProvider playedBy MethodsContentProvider {
	    
		@SuppressWarnings("basecall")
		callin Object[] getElements(Object element) {
	        if(!(element instanceof IType))
	            return NO_ELEMENTS;

	        Object[] result = null;
	        Object[] superResult = base.getElements(element);
	                
	        IType type = (IType)element;
	        IRoleType roleType = null;
	        
	        try {
	        	// for any OT type: filter generated:
		        int flags = type.getFlags();
		        if (TypeHelper.isTeam(flags) || TypeHelper.isRole(flags)) {
		        	superResult = Util.filterOTGenerated(superResult);
		        	if (TypeHelper.isRole(flags))
		        		roleType = (IRoleType)OTModelManager.getOTElement(type);
		        }
	        } catch (ClassCastException jme) {
	        	// nop (OTModelManager provided a non-role?)
	        } catch (JavaModelException jme) {
	        	// nop
	        }
	        ITypeHierarchy hierarchy = getHierarchyLifeCycle().getHierarchy();
	        List<Object> methodMappings = new ArrayList<Object>();
	        
	        if(   roleType != null
	           && getShowInheritedMethods() 
	           && hierarchy != null)
	        {
	        	// add inherited method mappings:
	            IType[] allSupertypes = hierarchy.getAllSupertypes(type);
				// sort in from last to first: elements with same name
				// will show up in hierarchy order 
				for (int idx = allSupertypes.length - 1; idx >= 0; idx--) 
				{
					IType superType = allSupertypes[idx];
					IOTType otEquiv = OTModelManager.getOTElement(superType);
					if (superType.exists() && otEquiv != null && otEquiv.isRole()) 
					{
					    IRoleType role = (IRoleType)otEquiv;
					    addAll(role.getMethodMappings(), methodMappings);
					}
				}
	        }
	        if(roleType != null && type.exists())
	        	// show own method mappings:
	            addAll(roleType.getMethodMappings(), methodMappings);
	        
	        result = new Object[superResult.length + methodMappings.size()];
	        System.arraycopy(superResult, 0, result, 0, superResult.length);
	        System.arraycopy(methodMappings.toArray(), 0, result, superResult.length, methodMappings.size());
	        return result;
	    }
	    getElements <- replace getElements;
	    
		private void addAll(Object[] array, List<Object> result) {
			if (array != null) 
				for (int idx = 0; idx < array.length; idx++) 
					result.add(array[idx]);
		}		
		
		@SuppressWarnings("decapsulation")
		TypeHierarchyLifeCycle getHierarchyLifeCycle() -> get TypeHierarchyLifeCycle fHierarchyLifeCycle;

		boolean getShowInheritedMethods() -> boolean isShowInheritedMethods();
	}
	
	/** 
	 * This role ensures that the traditional hierarchy view applies super class linearization
	 * for all classes above the focus type.
	 */
	protected class TraditionalHierarchyView playedBy TraditionalHierarchyContentProvider 
	{
		@SuppressWarnings("decapsulation")
		ITypeHierarchy getHierarchy() -> ITypeHierarchy getHierarchy();
	
		@SuppressWarnings("unchecked")
		getTypesInHierarchy <- replace getTypesInHierarchy;
		
		@SuppressWarnings("basecall")
		callin void getTypesInHierarchy(IType type, List<IType> res) {
			ITypeHierarchy hierarchy = getHierarchy();
			IType[] typesAboveFocus = null;
			if (hierarchy != null) {
				typesAboveFocus = OTTypeHierarchies.getInstance().getTypesInTraditionalHierarchy(hierarchy, type);
				if (typesAboveFocus != null) {
					res.addAll(Arrays.asList(typesAboveFocus));
					return;
				}
			} 
			base.getTypesInHierarchy(type, res);
		}
	}
}
