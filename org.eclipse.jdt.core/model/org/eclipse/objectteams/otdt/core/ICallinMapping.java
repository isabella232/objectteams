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
 * $Id: ICallinMapping.java 23416 2010-02-03 19:59:31Z stephan $
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
 * Specialized form of a IMethodMapping which provides information about
 * its kind and direct links to (possible) multiple base methods.
 * 
 * @author jwloka
 * @version $Id: ICallinMapping.java 23416 2010-02-03 19:59:31Z stephan $
 */
public interface ICallinMapping extends IMethodMapping
{
	public static final int KIND_BEFORE  = 1;
	public static final int KIND_AFTER   = 2;
	public static final int KIND_REPLACE = 3;
	
	/**
	 * Returns the kind of callin e.g. replace
	 * @return possible return values are: ICallinMapping.KIND_BEFORE
	 *                                     ICallinMapping.KIND_AFTER
	 *                                     ICallinMapping.KIND_REPLACE 
	 */
    public int getCallinKind();
    
    /** 
     * Returns whether at least one of the bound base method is captured including
     * overrides with covariant return types (marked as "RT+ bm()")
     */
    public boolean hasCovariantReturn();

	/**
	 * Dynamically resolves associated base methods from the JavaModel
	 * @return all JavaModel base method elements, at least there should
	 *         be one entry
	 */	
	public IMethod[] getBoundBaseMethods() throws JavaModelException;

	/**
     * Retrieve a handles for the base methods.
     * 
     * @return handles representing the base method specs 
     */
	public MethodData[] getBaseMethodHandles();
	
	/**
	 * Returns whether this callin mapping has a 'callin label'. If false, 
	 * getName() will return a generated label.
	 */
	public boolean hasName();

	/**
	 * Returns the label for this callin mapping. If hasName() returns false,
	 * a generated label is returned.
	 */
	public String getName();
}
