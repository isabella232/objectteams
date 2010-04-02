/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2005, 2007 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: IMethodMappingBinding.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.objectteams.otdt.core.compiler.InferenceKind;

/**
 * A callin/callout binding represents a callin-/callout method mapping
 * or a callout to field.
 * 
 * @author mkr
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMethodMappingBinding extends IBinding
{
	
	public String getName();
	
	/**
	 * Returns the type binding representing the role class
	 * that declares this callin-/callout mapping.
	 * 
	 * @return the binding of the role class that declares this mapping
	 */
	public ITypeBinding getDeclaringRoleClass();
    
    public ITypeBinding getReferencedBaseClass();
    
    public IMethodBinding getRoleMethod();

    public IMethodBinding[] getBaseMethods();
    
    public String[] getBaseArgumentNames();
    
    /**
     * @return true if this is a callin binding (vs. callout)
     */
    public boolean isCallin();

	/** Is this mapping an inferred callout? Which kind? */
	public InferenceKind getInferenceKind();
}
