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
 * $Id: OTModel.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;


import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.WeakHashSet;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;


/**
 * The Object Teams Model (OTM) main class. It provides an interface to
 * handle the underlying mapping for addition, change and removal of IOTTypes.
 * 
 * @see org.eclipse.objectteams.otdt.internal.core.OTTypeMapping
 * @see org.eclipse.objectteams.otdt.internal.core.CompilationUnitMapping 
 * @author jwloka
 * @version $Id: OTModel.java 23416 2010-02-03 19:59:31Z stephan $
 */
public class OTModel 
{
	private static OTModel _singleton;
	
	private OTTypeMapping _data = new OTTypeMapping();
	
	private WeakHashSet /*<IType>*/ _unopenedTypes = new WeakHashSet();
	
	protected OTModel()
	{
	}
	
	public static OTModel getSharedInstance()
	{
		if (_singleton == null)
		{
			_singleton = new OTModel();
		}
		
		return _singleton;
	}
	
	public static void dispose()
	{
        _singleton = null;
	}
	
	/** Register a type of which we don't yet have the flags. */
	public void addUnopenedType(IType type) {
		_unopenedTypes.add(type);
	}
	
	/**
	 * Add given element to the OTM.
	 */
	public void addOTElement(IOTType otType)
	{
		if (otType != null)
		{
			_data.put((IType)otType.getCorrespondingJavaElement(), otType);
		}
	}

	/**
	 * Indicates whether a JavaModel element has a corresponding OTM element
	 */	
	public boolean hasOTElementFor(IType type)
	{
		return _data.contains(type);
	}

	/**
	 * Returns associated OTM element for a given type if there is any.
	 * Also consider opening an _unopenedType on demand.
	 * 
	 * @return corresponding OTM element or null if no corresponding 
	 * element exists
	 */	
	public IOTType getOTElement(IType type)
	{
		IOTType result = _data.get(type);
		if (result != null)
			return result;
		// and now for on-demand opening (triggered, e.g., by getFlags()):
		if (type != null && type.isBinary() && this._unopenedTypes.contains(type)) 
		{
			this._unopenedTypes.remove(type);			
			try {
				return OTModelManager.getSharedInstance().addType(type, type.getFlags(), null, null, false);
			} catch (JavaModelException e) {
				// silently ignore: no success.
			}
		}
		return null;
	}
	
	/**
	 * Removes the given type. Removal depends on its delta state.
	 *  
	 * @param type       - element to remove
	 * @param hasChanged - false forces removal,
	 *                     true takes care about changed JavaModel elements
	 *                     since we neither know the original element nor
	 * 					   anything about its existence 
	 */	
	public void removeOTElement(IType type, boolean hasChanged)
	{
		if ((type != null) && (_data.contains(type)))
		{
			if (hasChanged)
			{
                _data.removeChangedElement(type);
			}
			else
			{
				_data.remove(type);
			}
		}
	}
}
