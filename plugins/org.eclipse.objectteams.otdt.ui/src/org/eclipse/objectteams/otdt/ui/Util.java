/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.objectteams.otdt.core.IOTJavaElement;
import org.eclipse.objectteams.otdt.core.IOTType;
import org.eclipse.objectteams.otdt.core.IRoleFileType;
import org.eclipse.objectteams.otdt.core.OTModelManager;


/**
 * @author kaiser
 * @version $Id: Util.java 15419 2007-02-23 12:07:05Z stephan $
 */
public class Util
{
	private static final Object[] NO_OBJECTS = new Object[0];

	/**
	 * Replaces IType inputs with IOTType if mapping exists in TypeManager
	 */        
	public static Object[] replaceOTTypes(Object[] types) {
		return replaceOTTypes(types, false);
	}
	public static Object[] replaceOTTypes(Object[] types, boolean lazyCopy)
	{	
		if (types == null)
			return NO_OBJECTS;
		
		boolean needCopy = lazyCopy;
		for (int idx = 0; idx < types.length; idx++)
		{
			Object curChild = types[idx];
            	
			if (curChild instanceof IType)
			{
				IOTType otElement =
					OTModelManager.getOTElement((IType)curChild);
        		        		
				if (otElement != null) {
					try {
						if (needCopy) {
							int len = types.length;
							System.arraycopy(types, 0, types=new IJavaElement[len], 0, len);
							needCopy = false;
						}
						types[idx] = otElement;
					} catch (ArrayStoreException ase) {
						int len = types.length;
						System.arraycopy(types, 0, types = new IJavaElement[len], 0, len);
						types[idx] = otElement;
					}
				} 
			}
		}
		return types; // due to arraycopy this might be different from the input
	}
	
	public static List<Object> removeExternalDefinedRoles(Object[] types)
	{
		ArrayList<Object> result = new ArrayList<Object>(types.length);
    	
		for (int idx = 0; idx < types.length; idx++)
		{
			Object curChild = types[idx];
	        	
			if (curChild != null && !(curChild instanceof IRoleFileType))
			{
				result.add(curChild);
			}
		}
		
		return result;
	}

	public static List<Object> filterOTGenerated(List<Object> children) {
		ArrayList<Object> result = new ArrayList<Object>(children.size());
		for (Object elem : children) {
			if (elem instanceof IJavaElement)
				if (isGenerated((IJavaElement)elem))
					continue;
			result.add(elem);
		}
		return result;
	}

	/* same as above, but array instead of list */
	public static Object[] filterOTGenerated(Object[] children) {
		if (children == null) return children;
		ArrayList<Object> result = new ArrayList<Object>(children.length);
		for (Object elem : children) { // Note: same code as above, different type!
			if (elem instanceof IJavaElement)
				if (isGenerated((IJavaElement)elem))
					continue;
			result.add(elem);
		}
		return result.toArray();
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
			// Predefined role types (non-overridable)?
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
}
