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
 * $Id: OTJavaElement.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.Member;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;


/**
 * OTJavaElement implementation.
 * 
 * @author jwloka
 * @version $Id: OTJavaElement.java 23416 2010-02-03 19:59:31Z stephan $
 */
public abstract class OTJavaElement extends Member implements IOTJavaElement
{
    // previously, a mapping and its referenced method had the same handle identifier.
    // However, cf.
    // -  JavaElement.JEM_METHOD et al.
    // -  SourceType.getHandleFromMemento()
    // -  org.eclipse.objectteams.otdt.metrics.core.internal.OTCore (unused!)

	// Used for handle identifier
    public static final char OTEM_METHODMAPPING = '`';
    public static final String METHODMAPPING = Character.toString(OTEM_METHODMAPPING);

	private int          _type;
	private List<IJavaElement>         _children;
	private IJavaElement _correspondingJavaElem;
	
	public OTJavaElement(int type, IJavaElement correspondingJavaElem, IJavaElement parent)
	{
		this(type, correspondingJavaElem, parent, true);
	}

	/**
	 * @param addAsChild Set this to false to avoid adding this element as a child of its parent, e.g. for temporary objects.
	 */
	public OTJavaElement(int type, IJavaElement correspondingJavaElem, IJavaElement parent, boolean addAsChild)
	{
		super((JavaElement) parent);
		_type     = type;
		_correspondingJavaElem = correspondingJavaElem;
		_children = new ArrayList<IJavaElement>();

		if (addAsChild && parent instanceof OTJavaElement)
			((OTJavaElement)parent).addChild(this);
	}

	public boolean hasChildren()
	{
	    return _children.size() > 0;
	}
	
	public IJavaElement[] getChildren()
	{
		if (this._children.isEmpty() && this._correspondingJavaElem instanceof IType)
			// fetch children from the java element (on first access):
			try {
				for (IJavaElement child : ((IType)this._correspondingJavaElem).getChildren())
					this._children.add(child);
			} catch (JavaModelException e) { /* noop */ }
		return _children.toArray( new IJavaElement[_children.size()] );
	}
	
	public void addChild(IOTJavaElement child)
	{
		if (child != null)
		{
			_children.add(child);
		}
	}

	public IJavaElement getParent()
	{
//{OTModelUpdate : if null return the parent of the wrapped java element
//orig:		return _parent;
	    IJavaElement result = this.parent;
	    
	    if (result == null)
	        result = getCorrespondingJavaElement().getParent();
	    
	    return result;
//jwl}	    
	}	

	public String getElementName()
	{
		return getCorrespondingJavaElement().getElementName();
	}

	public int getElementType()
	{
		return _type;
	}
	
	/**
	 * Assumes instanceof check is done in subclasses!
	 */
	public boolean equals(Object obj)
	{
		if (this == obj) 
		{ 
		    return true;
		}
		if(obj == null)
		{
		    return false;
		}

		OTJavaElement other = (OTJavaElement)obj;
		
		return getParent().equals(other.getParent())
                && (_type == other._type)
                && getCorrespondingJavaElement().equals(other.getCorrespondingJavaElement());
	}
	

	/**
	 * COPIED basically from JavaElement
	 * Returns the hash code for this Java element. By default,
	 * the hash code for an element is a combination of its name
	 * and parent's hash code. Elements with other requirements must
	 * override this method.
	 */
	public int hashCode() {
		IJavaElement parent = this.getParent();
		if (parent == null) return super.hashCode();
		return Util.combineHashCodes(getElementName().hashCode(), parent.hashCode());
	}

    @SuppressWarnings("nls")
	public String toString()
	{
		return "OTJavaElement " + getElementName();	
	}
	
    public IJavaElement getCorrespondingJavaElement()
    {
        return _correspondingJavaElem;
    }
    
	public OTJavaElement resolved(Binding binding) {
		return this;
	}

    public void setCorrespondingJavaElement(IJavaElement javaElem)
    {
        _correspondingJavaElem = javaElem;
    }

// delegated IJavaElement methods
    public boolean exists()
    {
        return _correspondingJavaElem.exists();
    }

    @Override
    public void close() throws JavaModelException {
    	super.close();
    	((JavaElement) this._correspondingJavaElem).close();
    }
    
    public IJavaElement getAncestor(int ancestorType)
    {
    	// first see if an OT element was requested (COPY&PASTE from JavaElement):
		IJavaElement element = this;
		while (element != null) {
			if (element.getElementType() == ancestorType)  return element;
			element= element.getParent();
		}
		// delegate to the java part:
        return _correspondingJavaElem.getAncestor(ancestorType);
    }

    public IResource getCorrespondingResource() throws JavaModelException
    {
    	// see SourceRefElement
    	if (!exists()) throw newNotPresentException();
    	return null;
    }

    public String getHandleIdentifier()
    {
        return _correspondingJavaElem.getHandleIdentifier();
    }

    public IJavaModel getJavaModel()
    {
        return _correspondingJavaElem.getJavaModel();
    }

    public IJavaProject getJavaProject()
    {
        return _correspondingJavaElem.getJavaProject();
    }

    public IOpenable getOpenable()
    {
        return _correspondingJavaElem.getOpenable();
    }

    public IPath getPath()
    {
        return _correspondingJavaElem.getPath();
    }

    public IJavaElement getPrimaryElement()
    {
        return _correspondingJavaElem.getPrimaryElement();
    }

    public IResource getResource()
    {
        return _correspondingJavaElem.getResource();
    }

    public ISchedulingRule getSchedulingRule()
    {
        return _correspondingJavaElem.getSchedulingRule();
    }

    public IResource getUnderlyingResource() throws JavaModelException
    {
        return _correspondingJavaElem.getUnderlyingResource();
    }

    public boolean isReadOnly()
    {
        return _correspondingJavaElem.isReadOnly();
    }

    public boolean isStructureKnown() throws JavaModelException
    {
        return _correspondingJavaElem.isStructureKnown();
    }
    
	@Override @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter)
    {
    	Object result = super.getAdapter(adapter);
    	if (result == null)
    		result = _correspondingJavaElem.getAdapter(adapter);
    	
    	return result;
    }
	
	public void toString(int tab, StringBuffer buffer) {
		for (int i = tab; i > 0; i--)
			buffer.append("  "); //$NON-NLS-1$
		buffer.append(this.toString());
	}
}
