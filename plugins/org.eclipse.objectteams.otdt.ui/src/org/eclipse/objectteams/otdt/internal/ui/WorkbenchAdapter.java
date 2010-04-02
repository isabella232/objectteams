/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2003, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: WorkbenchAdapter.java 23435 2010-02-04 00:14:38Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.objectteams.otdt.core.ICallinMapping;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.OTModelManager;
import org.eclipse.objectteams.otdt.ui.ImageConstants;
import org.eclipse.objectteams.otdt.ui.ImageManager;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * WorkbenchAdapter implementation for OTM elements.
 * 
 * @author kaiser
 * @version $Id: WorkbenchAdapter.java 23435 2010-02-04 00:14:38Z stephan $
 */
public class WorkbenchAdapter implements IWorkbenchAdapter
{
	public Object[] getChildren(Object parentElement)
	{
		try {
			if (parentElement instanceof ICompilationUnit) 
				return getCUMembers((ICompilationUnit)parentElement);

//{OTModelUpdate : children are in the java element: 
			if (parentElement instanceof IOTType)
				parentElement = ((IOTType)parentElement).getCorrespondingJavaElement();
// SH}
			if (parentElement instanceof IParent)	
				return ((IParent)parentElement).getChildren();
		} 
		catch (JavaModelException ex) { /* noop */ }

		return new Object[0];
	}

	/**
	 * Returns all types, including team and "external defined" role classes, of 
	 * a given ICompilationUnit.
	 */
	private Object[] getCUMembers(ICompilationUnit unit) throws JavaModelException
	{
		List<IType>    result = new ArrayList<IType>();
		IType[] types  = unit.getTypes();
		
		for (int idx = 0; idx < types.length; idx++)
		{
			if (OTModelManager.hasOTElementFor(types[idx]))
				result.add(OTModelManager.getOTElement(types[idx]));
			else
				result.add(types[idx]);
		}

		return result.toArray();
	}

	public ImageDescriptor getImageDescriptor(Object element)
	{
		ImageDescriptor result = ImageDescriptor.getMissingImageDescriptor();

		try
		{
			if (element instanceof IOTType)
				result = getTypeImageDescriptor((IOTType)element);
			else if (element instanceof IMethodMapping)
				result = getBindingImageDescriptor((IMethodMapping)element);
		}
		catch (JavaModelException ex)
		{
			// MissingImageDescriptor is already added to the result.
		}

		return result;
	}

    public String getLabel(Object elem)
	{
		if (elem instanceof IOTJavaElement)
			return ((IOTJavaElement)elem).getElementName();

		return null;
	}

    public Object getParent(Object elem)
	{
		if (elem instanceof IOTType)
		{
			IOTType otType = (IOTType)elem;
//{OTModelUpdate			
			return ((IType) otType.getCorrespondingJavaElement()).getParent();
//haebor}			
		}
		
		return null;
	}

	/**
	 * Return the propriate image descriptor for an OT type.
	 */
	private ImageDescriptor getTypeImageDescriptor(IOTType type) throws JavaModelException
	{
		if (type.isTeam())
		{
			if (type.isRole()) {
				if ((type.getFlags() & Flags.AccProtected) != 0)
					return ImageManager.getSharedInstance().getDescriptor(ImageConstants.TEAM_ROLE_PROTECTED_IMG);
				return ImageManager.getSharedInstance().getDescriptor(ImageConstants.TEAM_ROLE_IMG);
			}
			return ImageManager.getSharedInstance().getDescriptor(ImageConstants.TEAM_IMG);
		}
		else
		{
			if ((type.getFlags() & Flags.AccProtected) != 0)
				return ImageManager.getSharedInstance().getDescriptor(ImageConstants.ROLECLASS_PROTECTED_IMG);
			return ImageManager.getSharedInstance().getDescriptor(ImageConstants.ROLECLASS_IMG);
		}
	}
	
	private ImageDescriptor getBindingImageDescriptor(IMethodMapping binding)
	{
		if (binding.getMappingKind() == IMethodMapping.CALLIN_MAPPING)
		{
			ICallinMapping callinMapping = (ICallinMapping) binding;
			switch(callinMapping.getCallinKind())
			{
				case ICallinMapping.KIND_BEFORE:
					return ImageManager.getSharedInstance().getDescriptor(ImageConstants.CALLINBINDING_BEFORE_IMG);
				case ICallinMapping.KIND_AFTER:
					return ImageManager.getSharedInstance().getDescriptor(ImageConstants.CALLINBINDING_AFTER_IMG);
				default:
				case ICallinMapping.KIND_REPLACE:
					return ImageManager.getSharedInstance().getDescriptor(ImageConstants.CALLINBINDING_REPLACE_IMG);
			}
		}
		else
		{
			return ImageManager.getSharedInstance().getDescriptor(ImageConstants.CALLOUTBINDING_IMG);
		}		
	}	
}
