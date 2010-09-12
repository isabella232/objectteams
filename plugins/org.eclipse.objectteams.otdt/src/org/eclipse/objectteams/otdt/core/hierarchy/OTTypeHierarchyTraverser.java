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
package org.eclipse.objectteams.otdt.core.hierarchy;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.ICalloutMapping;
import org.eclipse.objectteams.otdt.core.ICalloutToFieldMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.hierarchy.TraverseRequestor.HierarchyContext;

/**
 * @author svacina
 * $Id: OTTypeHierarchyTraverser.java 23416 2010-02-03 19:59:31Z stephan $
 */

// TODO(jsv) use better class name
public class OTTypeHierarchyTraverser
{	
	public static final int TRAVERSE_IMPLICIT_FIRST = 2;
	public static final int TRAVERSE_EXPLICIT_FIRST = 3;

	
	private boolean _traverseImplicitFirst;
	private boolean _includeFocusType;
	private boolean _includeRootClass;
	
	private IType _focusType; 
	private ITypeHierarchy _hierarchy;
	private IProgressMonitor _progressMonitor;
	private TraverseRequestor _requestor;
	
	// TODO(jsv) change constructor 
	public OTTypeHierarchyTraverser(
			ITypeHierarchy hierarchy,
			TraverseRequestor requestor,
			boolean traverseImplicitFirst,
			boolean includeFocusType,
			boolean includeRootClass,
			IProgressMonitor progressMonitor)
	{
		_hierarchy = hierarchy;
		_requestor = requestor;
		_traverseImplicitFirst = traverseImplicitFirst;
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
		if (_hierarchy == null)
			_hierarchy = _focusType.newSupertypeHierarchy(_progressMonitor);
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
			report(_focusType, new HierarchyContext(true,false,false));
		}
		
		IType explicitSuperclass = null;
		IType[] implicitSuperclasses = null;
		
		IType[] superInterface = null;
		
		AdditionalTypeInfo currentTypeInfo = null;
		IType currentType = _focusType;
		ObjectQueue queue = new ObjectQueue();
		
		do
		{
			explicitSuperclass = OTTypeHierarchies.getInstance().getExplicitSuperclass(_hierarchy, currentType);
			implicitSuperclasses = OTTypeHierarchies.getInstance().getTSuperTypes(_hierarchy, currentType);
			superInterface = _hierarchy.getSuperInterfaces(currentType);
			
			if (explicitSuperclass != null && !_traverseImplicitFirst)
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
			
			if (explicitSuperclass != null && _traverseImplicitFirst)
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
					report(currentType, new HierarchyContext(false,
															currentTypeInfo.isExplicitSuperclass(),
															currentTypeInfo.isBehindExplicitInheritance()));
				}
			}
		} 
		while (currentTypeInfo != null);
	}

	private void report(IType type, HierarchyContext context) throws JavaModelException
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

