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
 * $Id: OTTypeHierarchyTraverser.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;

/**
 * @author svacina
 * $Id: OTTypeHierarchyTraverser.java 23416 2010-02-03 19:59:31Z stephan $
 */

// TODO(jsv) use better class name
public class OTTypeHierarchyTraverser
{	
	// only super hierarchy implemented
	public static final int SUB_HIERARCHY = 0;
	public static final int SUPER_HIERARCHY = 1;
	
	public static final int TRAVERSE_IMPLICIT_FIRST = 2;
	public static final int TRAVERSE_EXPLICIT_FIRST = 3;
	
	// context info
	public static final String IS_FOCUS_TYPE = "isFocusType"; //$NON-NLS-1$
	public static final String IS_EXPLICIT_SUPERCLASS = "isExplicitSuperclass"; //$NON-NLS-1$
	public static final String IS_BEHIND_EXPLICIT_INHERITANCE = "isBehindExplicitInheritance"; //$NON-NLS-1$

	
	private int _hierarchyToTraverse;
	private int _traverseFirst;
	private boolean _includeFocusType;
	private boolean _includeRootClass;
	
	private IType _focusType; 
	private OTTypeHierarchy _hierarchy;
	private IProgressMonitor _progressMonitor;
	private TraverseRequestor _requestor;
	
	// TODO(jsv) change constructor 
	public OTTypeHierarchyTraverser(
			TraverseRequestor requestor,
			int hierarchyToTraverse,
			int traverseFirst,
			boolean includeFocusType,
			boolean includeRootClass,
			IProgressMonitor progressMonitor)
	{
		_requestor = requestor;
		_hierarchyToTraverse = hierarchyToTraverse;
		_traverseFirst = traverseFirst;
		_includeFocusType = includeFocusType;
		_includeRootClass = includeRootClass;
		
		
		if (progressMonitor != null)
		{
			_progressMonitor = progressMonitor;
		}
		else
		{
			_progressMonitor = new NullProgressMonitor();
		}
	}
	
	private void initializeHierarchy() throws JavaModelException
	{
		_hierarchy = new OTTypeHierarchy(_focusType,
				_focusType.getJavaProject(),
				_hierarchyToTraverse == SUB_HIERARCHY);
		_hierarchy.refresh(_progressMonitor);
	}
	
	public void traverse() throws JavaModelException
	{
		assert _requestor != null : "Use only with valid requestor!"; //$NON-NLS-1$
		
      	_focusType = _requestor.getFocusType();        
        if (_focusType instanceof IOTType)
        {
        	_focusType = (IType)((IOTType)_focusType).getCorrespondingJavaElement();
        }
		
		initializeHierarchy();
		
		if (_includeFocusType)
		{
			report(_focusType, createContext(true,false,false));
		}
		
		IType explicitSuperclass = null;
		IType[] implicitSuperclasses = null;
		
		IType[] superInterface = null;
		
		AdditionalTypeInfo currentTypeInfo = null;
		IType currentType = _focusType;
		ObjectQueue queue = new ObjectQueue();
		
		do
		{
			explicitSuperclass = _hierarchy.getExplicitSuperclass(currentType);
			implicitSuperclasses = _hierarchy.getTSuperTypes(currentType);
			superInterface = _hierarchy.getSuperInterfaces(currentType);
			
			if (explicitSuperclass != null && _traverseFirst == TRAVERSE_EXPLICIT_FIRST)
			{
				queue.put(
						new AdditionalTypeInfo(
								explicitSuperclass,
								true,
								true));
			}
			
			for (int idx = 0; idx < implicitSuperclasses.length; idx++)
			{
				queue.put(
						new AdditionalTypeInfo(
								implicitSuperclasses[idx],
								false,
								currentTypeInfo == null ? false : currentTypeInfo.isBehindExplicitInheritance()));
			}
			
			if (explicitSuperclass != null && _traverseFirst == TRAVERSE_IMPLICIT_FIRST)
			{
				queue.put(
						new AdditionalTypeInfo(
								explicitSuperclass,
								true,
								true));
			}
			
			for (int idx = 0; idx < superInterface.length; idx++)
			{
				queue.put(
						new AdditionalTypeInfo(
						        superInterface[idx],
								false,
								currentTypeInfo == null ? false : currentTypeInfo.isBehindExplicitInheritance()));
			}
			
			currentTypeInfo = (AdditionalTypeInfo)queue.take();
			if (currentTypeInfo != null)
			{
				currentType = currentTypeInfo.getType();
				
				if (_includeRootClass || 
						!(currentType.getFullyQualifiedName().equals(TypeHelper.JAVA_LANG_OBJECT) ||
								currentType.getFullyQualifiedName().equals(TypeHelper.ORG_OBJECTTEAMS_TEAM)))
				{
					report(currentType,createContext(false,
							currentTypeInfo.isExplicitSuperclass(),  
							currentTypeInfo.isBehindExplicitInheritance()));
				}
			}
		} 
		while (currentTypeInfo != null);
	}
	
	private HashMap<String, Boolean> createContext(
			boolean isFocusType,
			boolean isExplicitSuperclass,
			boolean isBehindExplicitSuperclass)
	{
		HashMap<String, Boolean> context = new HashMap<String, Boolean>();
		context.put(IS_FOCUS_TYPE, new Boolean(isFocusType));
		context.put(IS_EXPLICIT_SUPERCLASS, new Boolean(isExplicitSuperclass));
		context.put(IS_BEHIND_EXPLICIT_INHERITANCE, new Boolean(isBehindExplicitSuperclass));
		return context;
	}
	
	
	private void report(IType type, HashMap<String, Boolean> context) throws JavaModelException
	{
		IOTType otType = OTModelManager.getOTElement(type);
		
		//TODO(jsv): should we report the OTType if possible?
		// report _type
		_requestor.report(type,context);
		// report methods
		for (int idx = 0; idx < type.getMethods().length; idx++)
		{
			_requestor.report(type.getMethods()[idx], context);
		}
		
		// report fields 
		for (int idx = 0; idx < type.getFields().length; idx++)
		{
			_requestor.report(type.getFields()[idx],context);
		}
		
		// report callin / callout if possible
		if (otType != null && otType instanceof IRoleType )
		{
			IRoleType roleType = (IRoleType)otType;
			// report callin
			for (int idx = 0; idx < roleType.getMethodMappings(IRoleType.CALLINS).length; idx++)
			{
				_requestor.report((ICallinMapping)roleType.getMethodMappings(IRoleType.CALLINS)[idx],context);
			}
			
			//report callout
			for (int idx = 0; idx < roleType.getMethodMappings(IRoleType.CALLOUTS).length; idx++)
			{
				IMethodMapping mapping = roleType.getMethodMappings(IRoleType.CALLOUTS)[idx];
				if (mapping instanceof ICalloutToFieldMapping)
				{
					_requestor.report((ICalloutToFieldMapping)mapping,context);
				}
				else
				{
					_requestor.report((ICalloutMapping)mapping,context);
				}
			}
		}
	}
}

class AdditionalTypeInfo
{
	private IType _type;
	private boolean _isExplicitSuperclass;
	private boolean _isBehindExplicitInheritance;
	
	public AdditionalTypeInfo(IType type, 
			boolean isExplicitSuperclass, 
			boolean isBehindExplicitInheritance)
	{
		this._type = type;
		this._isBehindExplicitInheritance = isBehindExplicitInheritance;
		this._isExplicitSuperclass = isExplicitSuperclass;
	}
	
	public boolean isExplicitSuperclass(){
		return _isExplicitSuperclass;
	}
	
	public boolean isBehindExplicitInheritance(){
		return _isBehindExplicitInheritance;
	}
	
	public IType getType()
	{
		return _type;
	}
	
	@SuppressWarnings("nls")
	public String toString()
	{
		return "ExplicitSuperclass = " + _isExplicitSuperclass + "\n" +
			"BehindExplicitInheritance = " + _isBehindExplicitInheritance + "\n" +
			_type.toString();
	}
}

class ObjectQueue
{
	private ArrayList<Object> _list;
	public ObjectQueue()
	{
		_list = new ArrayList<Object>();
	}
	
	public void put(Object o)
	{
		_list.add(o);
	}
	
	public Object take()
	{
		return _list.size() <= 0 ? null : _list.remove(0);
	}

	public boolean isEmpty()
	{
		return _list.size() == 0;
	}
	
	public int size()
	{
	    return _list.size();
	}
}

