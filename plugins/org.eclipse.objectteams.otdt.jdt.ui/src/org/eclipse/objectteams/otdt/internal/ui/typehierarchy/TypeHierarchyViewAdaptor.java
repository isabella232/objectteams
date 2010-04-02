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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.CompositeActionGroup;
import org.eclipse.jdt.internal.ui.typehierarchy.MethodsViewer;
import org.eclipse.jdt.internal.ui.typehierarchy.ToggleViewAction;
import org.eclipse.jdt.ui.ITypeHierarchyViewPart;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;
import org.eclipse.objectteams.otdt.ui.Util;

import base org.eclipse.jdt.internal.ui.typehierarchy.HierarchyLabelProvider;
import base org.eclipse.jdt.internal.ui.typehierarchy.MethodsContentProvider;
import base org.eclipse.jdt.internal.ui.typehierarchy.MethodsLabelProvider;
import base org.eclipse.jdt.internal.ui.typehierarchy.TypeHierarchyLifeCycle;
import base org.eclipse.jdt.internal.ui.typehierarchy.TypeHierarchyViewPart;
import base org.eclipse.jdt.internal.ui.typehierarchy.SubTypeHierarchyViewer.SubTypeHierarchyContentProvider;

/**
 * This team adapts the type hierarchy to handle role inheritance correctly.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public team class TypeHierarchyViewAdaptor 
{
	/** Manipulate set of available actions. */
	@SuppressWarnings("decapsulation")
	protected class ViewPart playedBy TypeHierarchyViewPart 
	{
		void changeViewActions() 
		{
			// remove first action (VIEW_ID_TYPE not applicable in the OT hierarchy)
			ToggleViewAction[] actions = getViewActions();
			setViewActions(new ToggleViewAction[] { actions[1], actions[2] });
		}
		changeViewActions <- before createPartControl;

		ToggleViewAction[] getViewActions()               -> get ToggleViewAction[] fViewActions;
		void setViewActions(ToggleViewAction[] actions)   -> set ToggleViewAction[] fViewActions;
		
		CompositeActionGroup getActionGroups()            -> get CompositeActionGroup fActionGroups;
		void setActionGroups(CompositeActionGroup groups) -> set CompositeActionGroup fActionGroups;
		
		// ensure the old default value CLASSIC never comes through:
		callin void setHierarchyMode(int viewerIndex) {
			if (viewerIndex == ITypeHierarchyViewPart.HIERARCHY_MODE_CLASSIC)
				viewerIndex = ITypeHierarchyViewPart.HIERARCHY_MODE_SUPERTYPES;
			base.setHierarchyMode(viewerIndex);
		}
		void setHierarchyMode(int viewerIndex) <- replace void setHierarchyMode(int viewerIndex);
	}
	
	/** If element is a role type, ensure an OTTypeHierarchy is created. */
	protected class LifeCycle playedBy TypeHierarchyLifeCycle 
	{
		@SuppressWarnings("basecall")
		callin ITypeHierarchy createTypeHierarchy(IJavaElement element, IProgressMonitor pm) throws JavaModelException {
			if (element.getElementType() == IJavaElement.TYPE) {
				// compute a hierarchy that can handle teams and roles    
			    IOTType otType = OTModelManager.getOTElement((IType) element);
			    if (otType != null) {
				    if (getIsSuperTypesOnly())
				        return otType.newSuperOTTypeHierarchy(pm);
				    else
				        return otType.newOTTypeHierarchy(pm);
			    }		  
			}
			return base.createTypeHierarchy(element, pm);
		}
		@SuppressWarnings("decapsulation")
		createTypeHierarchy <- replace createTypeHierarchy;
		
		@SuppressWarnings("decapsulation")
		boolean getIsSuperTypesOnly() -> get boolean fIsSuperTypesOnly;
		
		protected
		ITypeHierarchy getHierarchy() -> ITypeHierarchy getHierarchy();
	}
	
	
	/** Refuse to answer _the_ super class for a role type. 
	 *  Return null rather throwing an exception as OTTypeHierarchy would do. */
	protected class SubTypeHierarchyContentProvider playedBy SubTypeHierarchyContentProvider 
	{
		@SuppressWarnings("basecall")
		callin Object getParent(Object element) {
			if(element instanceof IType) {
				IOTType otType = OTModelManager.getOTElement((IType)element);
				if (otType != null && otType.isRole())
					return null; // not a single parent
			}
			return base.getParent(element);
		}
		getParent <- replace getParent;
	}
	
	/**
	 * Ensure role names are always prefixed upto and including the enclosing team type. 
	 */
	public class HierarchyLabelProvider  playedBy HierarchyLabelProvider 
	{
		@SuppressWarnings("basecall")
		callin String getText(Object element) {
	        IOTType otType = null;
			if(element instanceof IType)
			{
			    IType type = (IType)element;
			    if(OTModelManager.hasOTElementFor(type)) {
			    	otType = OTModelManager.getOTElement((IType)element);
			    } else {
			        IType encType = type.getDeclaringType();
			        IType classPart = 
			            encType == null ? null : encType.getType(IOTConstants.OT_DELIM + type.getElementName());
			        if (classPart != null && OTModelManager.hasOTElementFor(classPart))
			            otType = OTModelManager.getOTElement(classPart);
			    }
			}
			if(otType != null) {    	
			    //team or role
			    StringBuffer text = new StringBuffer();
				computeNames(text, otType);
				return text.toString();
			} else {
			    //ordinary type
				return base.getText(element);
			}

		}
		getText <- replace getText;
	}

	/**
	 * Different presentation for role methods, if defining type should be displayed.
	 * Also support method mappings.
	 */
	protected class MethodsLabelProvider playedBy MethodsLabelProvider {

	    callin String getText(Object element) {
	    	String text;
	    	boolean isRole = false;
	    	if (element instanceof IMember) { 
	    		IType declaringType = ((IMember)element).getDeclaringType();
	    		IOTType otType = OTModelManager.getOTElement(declaringType);
                if(otType != null && otType instanceof IRoleType)
                	isRole = true;
	    	}
	        if(isRole && isShowDefiningType())  {
	        	// get the text without defining type, because we need to
	        	// compute the defining type for roles ourselves:
	            setShowDefiningType(false);
	            text= base.getText(element);
	            setShowDefiningType(true);

	            try {
	            	// compute and assemble the defining type name:
	                IType type = otGetDefiningType(element);
		            if(type != null) {
		                StringBuffer extText = new StringBuffer();
		                IOTType otEquiv = isRole ? 
		                		OTModelManager.getOTElement(type) :
		                		null;
		                if(otEquiv != null && otEquiv instanceof IRoleType) {
		                	// prepend qualified role name:
		                    computeNames(extText, otEquiv);
		                } else {
		                	extText.append(type.getElementName());
		                }
		                extText.append(JavaElementLabels.CONCAT_STRING);
		                extText.append(text);
		                return extText.toString();
		            }
	            }
	            catch (JavaModelException exc) { /* Just use the text we already have. */ }
	        } else {
	        	text= base.getText(element);
	        }
	        
	        if (element instanceof IMethodMapping && getShowInherited() && !isShowDefiningType()) {
	        	// this case is not handled above:
	        	// method mappings needing to show their declaring type.
	            StringBuffer extText = new StringBuffer();
	            extText.append(text);
	            extText.append(JavaElementLabels.CONCAT_STRING);
	            extText.append(((IMethodMapping)element).getDeclaringType().getFullyQualifiedName('.'));
	            return extText.toString();
	        }
	            
	        return text;
	    }
	    getText <- replace getText;
	    
	    
	    private IType otGetDefiningType(Object element) throws JavaModelException 
		{
	        if (element instanceof IOTJavaElement) { 
		        switch (((IOTJavaElement)element).getElementType()) {
		        case IOTJavaElement.CALLIN_MAPPING:
		        case IOTJavaElement.CALLOUT_MAPPING:
		        case IOTJavaElement.CALLOUT_TO_FIELD_MAPPING:
		        	// FIXME(SH): inconsistency: should search the initial defining type instead:
		        	return ((IMethodMapping)element).getDeclaringType();
		        }
	        }
	        return getDefiningType(element);
		}

	    abstract IType getDefiningType(Object element) throws JavaModelException;
	    
	    @SuppressWarnings("decapsulation")
		IType getDefiningType(Object element)              -> IType getDefiningType(Object element);
	    void setShowDefiningType(boolean showDefiningType) -> void setShowDefiningType(boolean showDefiningType);
	    boolean isShowDefiningType()                       -> boolean isShowDefiningType();
	    
	    @SuppressWarnings("decapsulation")
	    boolean getShowInherited() -> get MethodsViewer fMethodsViewer
	    	with {          result <- fMethodsViewer.isShowInheritedMethods() }
	}

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
		LifeCycle getHierarchyLifeCycle() -> get TypeHierarchyLifeCycle fHierarchyLifeCycle;

		boolean getShowInheritedMethods() -> boolean isShowInheritedMethods();
	}
	
	
	/**
	 * Helper for two roles: create a role name prefixed with its team
	 * (and all intermediate roles, if nested). 
	 */
    private static StringBuffer computeNames(StringBuffer buffer, IOTType type)
    {
        IOTType curType = type;
        while(curType.isRole())
        {
            buffer.insert(0, curType.getElementName());
            buffer.insert(0, '.');
            curType = ((IRoleType)curType).getTeam();
            if (curType == null) 
            	return buffer; // avoid NPE, leave incomplete name if needed
        }
        return buffer.insert(0, curType.getElementName());
    }
}
