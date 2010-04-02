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
 * $Id: OTJavaElementDelta.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Jochen
 *
 */
public class OTJavaElementDelta implements IJavaElementDelta 
{
	private IJavaElementDelta _delegate;
	private Map<IJavaElement,IOTJavaElement>	_otElemMapping;

	public OTJavaElementDelta(IJavaElementDelta delta)
	{
		this(new HashMap<IJavaElement,IOTJavaElement>(), delta);//intentionally empty HashMap!
	}

	/**
	 * @param otElemMapping
	 * @param delta
	 */
	public OTJavaElementDelta(Map<IJavaElement,IOTJavaElement> otElemMapping, IJavaElementDelta delta) {
		
		_delegate = delta;
		_otElemMapping = otElemMapping;
	}

	public IJavaElement getElement() 
	{
		return maybeTransformElement(_delegate.getElement());
	}
	
	public IJavaElementDelta[] getAddedChildren() 
	{
		return wrapChildren(_delegate.getAddedChildren());
	}
	
	public IJavaElementDelta[] getAffectedChildren() 
	{
		return wrapChildren(_delegate.getAffectedChildren());
	}
	
	public IJavaElementDelta[] getChangedChildren() 
	{
		return wrapChildren(_delegate.getChangedChildren());
	}
	
	
	public IJavaElement getMovedFromElement() 
	{
		return maybeTransformElement(_delegate.getMovedFromElement());
	}
	
	public IJavaElement getMovedToElement() 
	{
		return maybeTransformElement(_delegate.getMovedToElement());
	}
	
	public IJavaElementDelta[] getRemovedChildren() 
	{
		return wrapChildren(_delegate.getRemovedChildren());
	}
	
	public IJavaElementDelta[] getAnnotationDeltas() {
		return wrapChildren(_delegate.getAnnotationDeltas());
	}

//pure delegates	
	public IResourceDelta[] getResourceDeltas() 
	{
		return _delegate.getResourceDeltas();
	}
	
	public String toString() 
	{
		return _delegate.toString();
	}
	
	public int getFlags() 
	{
		return _delegate.getFlags();
	}
	
	public int getKind() 
	{
		return _delegate.getKind();
	}

	public CompilationUnit getCompilationUnitAST() 
	{
		return _delegate.getCompilationUnitAST();
	}
	
	
//end of delegates
//helpers
	private OTJavaElementDelta[] wrapChildren(IJavaElementDelta[] deltas)
	{
		OTJavaElementDelta[] result = wrapDeltas(deltas);
		
		return result;
	}
	
	private OTJavaElementDelta[] wrapDeltas(IJavaElementDelta[] deltas)
	{
		OTJavaElementDelta[] result = new OTJavaElementDelta[deltas.length];
		for(int idx = 0; idx < deltas.length; idx++)
		{
			result[idx] = new OTJavaElementDelta(_otElemMapping, deltas[idx]);
		}
		return result;
	}
	
	private IJavaElement maybeTransformElement(IJavaElement element)
	{
		if(element instanceof IType)
		{
			IOTJavaElement otElement = 
				OTModelManager.getOTElement((IType)element);
			if(otElement != null)
			{
				return (IJavaElement) otElement;
			}
			else
			{
				IOTJavaElement otElem = _otElemMapping.get(element);
				if (otElem != null)
				{
					return otElem;
				}
			}
		}
		return element;
	}
}
