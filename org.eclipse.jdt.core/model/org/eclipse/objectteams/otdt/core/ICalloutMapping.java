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
 * $Id: ICalloutMapping.java 23416 2010-02-03 19:59:31Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.core.util.MethodData;


/**
 * Specialized form of a IMethodMapping which provides a direct link to
 * its base method.
 * 
 * @author jwloka
 * @version $Id: ICalloutMapping.java 23416 2010-02-03 19:59:31Z stephan $
 */
public interface ICalloutMapping extends IMethodMapping
{
	/**
	 * Dynamically resolves the associated base method from the JavaModel
	 * @return a base method JavaModel element
	 */	
	public IMethod getBoundBaseMethod() throws JavaModelException;
	
	/**
     * Retrieve a handle for the base method.
     * 
     * @return handle representing the base method spec 
     */
	public MethodData getBaseMethodHandle();
	
    /**
     * Is this a callout-override ('=>')?
     */
    public boolean isOverride();
    
    /** modifiers if they have been specified in source code, otherwise 0. */
    public int getDeclaredModifiers();
}
