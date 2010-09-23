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
 * $Id: ContentProvider.java 23438 2010-02-04 20:05:24Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
team package org.eclipse.objectteams.otdt.internal.ui.packageview.PackageExplorerAdaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleFileType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.ui.OTDTUIPlugin;
import org.eclipse.objectteams.otdt.ui.Util;

/**
 * @author stephan
 *
 */
@SuppressWarnings("restriction")
protected class ContentProvider playedBy PackageExplorerContentProvider 
{
    static final String ERROR_MESSAGE =
		"View 'OT Package Explorer' failed to access JavaModel!"; //$NON-NLS-1$
    
	static final String PREFERENCE_KEY = "PackageExplorerHideRolePackages.isChecked"; //$NON-NLS-1$

    private boolean _hideRolePackages = false;

    protected ContentProvider(PackageExplorerContentProvider myBase) {
    	this._hideRolePackages = OTDTUIPlugin.getDefault().getPreferenceStore().getBoolean(PREFERENCE_KEY);
    }

	public void setHideRolePackages(boolean enable)
    {
        this._hideRolePackages = enable;
    	OTDTUIPlugin.getDefault().getPreferenceStore().setValue(PREFERENCE_KEY, enable);
    }
    
    public boolean isHideRolePackages()
    {
        return this._hideRolePackages;
    }
        
    @SuppressWarnings("basecall")
	callin Object[] getChildren(Object parentElement) 
    {
        List<Object> children = null;
		if (parentElement instanceof IOTType)
		{
			IJavaElement javaParentElement = ((IOTJavaElement)parentElement).getCorrespondingJavaElement();
			children = new ArrayList<Object>(Arrays.asList(getChildrenAdapted(javaParentElement)));
			if (isHideRolePackages())
			{
				IType[] roleFiles = null;
                try
                {
                    roleFiles = ((IOTType)parentElement).getRoleTypes(IOTType.ROLEFILE);
                    Util.replaceOTTypes(roleFiles);
                }
                catch (JavaModelException ex)
                {
                    OTDTUIPlugin.getExceptionHandler().logCoreException(ERROR_MESSAGE, ex);
                }
                children.addAll(Arrays.asList(roleFiles));
			}
			children = filterOTGenerated(children);
			return children.toArray();
		}

    	// no OT element -> delegate to original content provider
		children = new ArrayList<Object>(Arrays.asList(base.getChildren(parentElement)));
    	
	    //role packages invisible?
		if (isHideRolePackages())
		{        		        		
			try
			{
			    filterExternalRolePackage(parentElement, children);
			}
			catch (JavaModelException ex)
			{
				OTDTUIPlugin.getExceptionHandler().logCoreException(ERROR_MESSAGE, ex);
			}
		}
	    return Util.replaceOTTypes(children.toArray());
    }
    getChildren <- replace getChildren;

	@SuppressWarnings("basecall")
	callin Object getParent(Object element)
    {
		if (this._hideRolePackages && element instanceof IType) {
			// short cut for role file who's parent in this mode is the team
			IOTType ottype = OTModelManager.getOTElement((IType)element);
			if (ottype instanceof IRoleType)
				return ((IRoleType) ottype).getTeam();
		}
    	return base.getParent(element);
    }
	getParent <- replace getParent;

    Object[] getChildrenAdapted(Object element) {
    	 IWorkbenchAdapter adapter = getAdapter(element);
         if (adapter != null) {
             Object[] result = adapter.getChildren(element);
             return Util.replaceOTTypes(result, true/*lazyCopy*/);
         }
         return new Object[0];
    }

    protected List<Object> filterOTGenerated(List<Object> children) {
    	ArrayList<Object> result = new ArrayList<Object>(children.size());
    	for (Iterator<Object> iter = children.iterator(); iter.hasNext();) {
    		Object elem = iter.next();
    		if (elem instanceof IJavaElement)
    		{
    			IJavaElement javaElem = (IJavaElement)elem;
    			if (!isGenerated(javaElem)) 
				{
					result.add(javaElem);
				}
    		} else 
    		{
    			result.add(elem);
//        		if (elem instanceof IJavaElement)
//        			System.out.println("added: "+((IJavaElement)elem).getElementName()+":"+((IJavaElement)elem).getElementType());
    		}
		}
    	return result;
    }
    
    @SuppressWarnings("nls")
	public static boolean isGenerated(IJavaElement elem) {
		// TODO (SH): check whether ViewerFilters can do the job better.
		
		// all kinds of generated features determined by name:
    	String name = elem.getElementName();    	
		final String[] patterns = new String[] {
    			"_OT$",	"TSuper__OT__",	             // general OT-prefix 
				"class$", "access$", "val$", "this$" // std. java synthetics.
				};
    	for (int i = 0; i < patterns.length; i++) {
			if (name.indexOf(patterns[i]) >= 0)
				return true;
		}
		
    	switch (elem.getElementType()) {
    	case IJavaElement.TYPE: 
    	case IOTJavaElement.ROLE:
			// Predifined role types (non-overridable)?
	    	final String[] fullPatterns = new String[] {
				"IConfined", "Confined", "__OT__Confined", "ILowerable",	// special OT types    	
	    	};
	    	for (int i = 0; i < fullPatterns.length; i++) {
				if (name.equals(fullPatterns[i]))
					return true;
			}
	    	break;
	    case IJavaElement.METHOD:
	    	// tsuper-method?
	    	IMethod method = (IMethod)elem;
	    	String[] paramTypes = method.getParameterTypes();
	    	if (paramTypes.length > 0) {
	    		String lastType = Signature.getSimpleName(Signature.toString(paramTypes[paramTypes.length-1]));
	    		if (lastType.startsWith("TSuper__OT__"))
	    			return true;
	    	}
	    	break;
    	}
		
		// Synthetic role interface?
    	if (elem.getElementType() == IOTJavaElement.ROLE) {
    		IType type = (IType)elem;
    		try {
    			if (Flags.isSynthetic(type.getFlags()))
    				return true;
    		} catch (JavaModelException ex) {
    			// nop
    		}
    	}
   		return false;
    }
	/**
	 * Remove packages containing external roles from given list
	 */
	private void filterExternalRolePackage(Object parentElement, List<Object> children)
		throws JavaModelException
	{
	    ArrayList<Object> removalList = new ArrayList<Object>(children.size());
        Iterator<Object> iterator = children.iterator();
        Object element;
        while (iterator.hasNext())
	    {
            element = iterator.next();
	        if(isExternalRolePackage(element))
	        {
	            removalList.add(element);
	        }
	    }
	    Iterator<Object> removalIterator = removalList.iterator();
	    while(removalIterator.hasNext())
	    {
	        children.remove(removalIterator.next());
	    }
//TODO (haebor) : consider layoutmode 	    
	}

	// copied from BaseWorkbenchContentProvider
	IWorkbenchAdapter getAdapter(Object element) {
        if (!(element instanceof IAdaptable)) {
            return null;
        }
        return (IWorkbenchAdapter) ((IAdaptable) element)
                .getAdapter(IWorkbenchAdapter.class);
	}

	@SuppressWarnings({ "decapsulation", "rawtypes"/*Collection*/ })
	void findTeamCU(Object root)
	<- replace
	void postRefresh(Object root, int relation, Object affectedElement, Collection runnables)
		when (_hideRolePackages);

	callin void findTeamCU(Object element) {
		if (element instanceof CompilationUnit) {
			// when refreshing in logical view, rofi-CUs are not visible, replace element by the enclosing Team CU
			CompilationUnit cu = (CompilationUnit) element;
			try {
				IType[] types = cu.getTypes();
				if (types != null && types.length > 0) {
					IOTType ottype = OTModelManager.getOTElement(types[0]);
					if (ottype instanceof IRoleFileType) {
						// search outermost team:
						while(ottype.isRole())
							ottype = ((IRoleType)ottype).getTeam();
						element = ottype.getCompilationUnit();
					}
				}
			} catch (JavaModelException jme) {
				// nop, just keep element unchanged
			}
		}
		base.findTeamCU(element);
	}	
}
