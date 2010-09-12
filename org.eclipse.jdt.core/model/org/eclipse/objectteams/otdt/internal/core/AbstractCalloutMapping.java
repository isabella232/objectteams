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
 * $Id: AbstractCalloutMapping.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.IMethodMapping;
import org.eclipse.objectteams.otdt.core.util.MethodData;

/** 
 * Common super-class of callout and callout-to-field. 
 * Purpose: implement a view of this mapping that can be used for searching.
 * This view pretends to be a method 
 * (actually short-hand callouts indeed are translated to a method ;-)
 *  
 * @author stephan
 */
public abstract class AbstractCalloutMapping extends MethodMapping implements IMethod {

	protected boolean         _mimicMethodDecl = false;
    protected IMethodMapping  _originalMethodMapping; // only for stealth method mappings

	public AbstractCalloutMapping(int declarationSourceStart, 
								  int sourceStart, 
								  int sourceEnd, 
								  int declarationSourceEnd, 
								  int        elementType, 
								  IMethod    corrJavaMethod, 
								  IType      parentRole, 
								  MethodData roleMethodHandle, 
								  boolean    hasSignature) 
	{
		super(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, 
			  elementType, corrJavaMethod, parentRole, 
			  roleMethodHandle, 
			  hasSignature);
	}

	public AbstractCalloutMapping(int declarationSourceStart, 
								  int sourceStart, 
								  int sourceEnd, 
								  int declarationSourceEnd, 
								  int elementType, 
								  IMethod corrJavaMethod,
								  IType parentRole, 
								  MethodData roleMethodHandle, 
								  boolean hasSignature, 
								  boolean addAsChild) 
	{
		super(declarationSourceStart, sourceStart, sourceEnd, declarationSourceEnd, 
				  elementType, corrJavaMethod, parentRole, 
				  roleMethodHandle, 
				  hasSignature, addAsChild);
	}

	public abstract IMethodMapping createStealthMethodMapping();
	
    public boolean isStealthMethodMapping()
    {
        return getElementType() == IJavaElement.METHOD;
    }
    
    public IMethodMapping getOriginalMethodMapping()
    {
        if (isStealthMethodMapping())
	        return _originalMethodMapping;
        return this;
    }

	public IMemberValuePair getDefaultValue() throws JavaModelException {
		// callout mappings are not used in annotation types ;-)
		return null;
	}
}
