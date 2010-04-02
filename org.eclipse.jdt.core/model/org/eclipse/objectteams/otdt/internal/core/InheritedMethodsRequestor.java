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
 * $Id: InheritedMethodsRequestor.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.util.HashMap;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.TypeHelper;
import org.eclipse.objectteams.otdt.core.compiler.IOTConstants;

/**
 * @author svacina
 */

public class InheritedMethodsRequestor extends TraverseRequestor
{
	private HashMap<String, IMethod> _result;
	private boolean _overwriteCollectedMethods;
	private boolean _checkVisibility;
	
	// TODO (jsv): can this class cope with IOTTypes as well? If not, it should use its correspondingJavaElement()
	public InheritedMethodsRequestor(IType type,
			boolean overwriteCollectedMethods,
			boolean checkVisibility)
	{
		_result = new HashMap<String, IMethod>(); 
		_focusType = type;
		_overwriteCollectedMethods = overwriteCollectedMethods;
		_checkVisibility = checkVisibility;
	}
	
	public void report(IType type, HashMap<String,Boolean> context)
	{
		boolean isFocusType = ((Boolean)context.get(OTTypeHierarchyTraverser.IS_FOCUS_TYPE)).booleanValue();
		boolean isExplicitSuperclass = ((Boolean)context.get(OTTypeHierarchyTraverser.IS_EXPLICIT_SUPERCLASS)).booleanValue();
		boolean isBehindExplicitInheritance = ((Boolean)context.get(OTTypeHierarchyTraverser.IS_BEHIND_EXPLICIT_INHERITANCE)).booleanValue();
		boolean checkVisibility;
		
		try
		{
			if (!_checkVisibility)
			{
				checkVisibility = false;
			}
			else if (type.isInterface())
		    {
		        if (_focusType.isInterface())
		        {
		            checkVisibility = false;
		        }
		        else
		        {
		            return;
		        }
		    }
		    else
		    {
		        if (isFocusType)
		        {
		            checkVisibility = false;
		        }
		        else
		        {
		            checkVisibility = isExplicitSuperclass || isBehindExplicitInheritance;
		        }
		    }
		    writeMethodsToResult(type,checkVisibility);
		} 
		catch (JavaModelException e)
		{
			// TODO(jsv)handle exception
			e.printStackTrace();
		}
	}
	
	private void writeMethodsToResult(
			IType type, 
			boolean checkVisibility) throws JavaModelException
	{
		IMethod[] methods = type.getMethods();
		IMethod currMethod = null;
		
		for (int methodIdx = 0; methodIdx < methods.length; methodIdx++)
        {
			currMethod = methods[methodIdx];
            if (currMethod.getElementName().startsWith(IOTConstants.OT_DOLLAR))
            {
                continue;
            }
            
            if (currMethod.isConstructor())
            {
                continue;
            }
            // TODO(jsv) check completeness
            if (checkVisibility)
            {
            	// private
            	if (Flags.isPrivate(currMethod.getFlags()))
            	{
            		continue;
            	} 
            	// public , protected
            	else if (Flags.isPublic(currMethod.getFlags()) || 
            			Flags.isProtected(currMethod.getFlags()) )
            	{
            		storeMethod(currMethod);
            	}
            	// default
            	else
            	{
            		if (type.getPackageFragment().equals(_focusType.getPackageFragment()))
            		{
            			storeMethod(currMethod);
            		}
            		else
            		{
            			continue;
            		}
            	}
            }
            else
            {
            	storeMethod(currMethod);
            }
        }
	}
	
	private void storeMethod(IMethod method)
	{
		String key = TypeHelper.getMethodSignature(method);
		
		if (!_overwriteCollectedMethods)
		{
			if (!_result.containsKey(key))
			{
				_result.put(key, method);
			}
		}
		else
		{
			_result.put(key, method);
		}
	}
	
	public IMethod[] getResult()
	{
		return _result.values().toArray(new IMethod[_result.size()]);	
	}
}
