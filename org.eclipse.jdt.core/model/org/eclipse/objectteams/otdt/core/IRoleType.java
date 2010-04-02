/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2009 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute for Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: IRoleType.java 23417 2010-02-03 20:13:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Fraunhofer FIRST - Initial API and implementation
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.core;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;


/**
 * Refined IOTType to represent behaviour of roles.
 *
 * @author jwloka
 * @version $Id: IRoleType.java 23417 2010-02-03 20:13:55Z stephan $
 */
public interface IRoleType extends IOTType
{
	/**
	 * Return role's team
	 */
    public IOTType getTeam();
    
    /**
     * Return a role's team, use the plain Java representation.
     */
    public IType getTeamJavaType();

	/**
	 * Returns all method mappings defined in this role
	 * @return array of mappings or empty array
	 */
	public IMethodMapping[] getMethodMappings();

	// Note: powers of 2!
	public static int CALLINS = 1;
	public static int CALLOUTS = 2;
	
	/**
	 * Convenience method returns all method mappings of a given type
	 * defined in this role
	 * @param type - an ORed combination of CALLINS and CALLOUTS
	 * @return array of mappings or empty array
	 */
	public IMethodMapping[] getMethodMappings(int type);

	/**
	 * Dynamically resolves role's base class declared by "playedBy"
	 * @throws JavaModelException if resolving fails
	 * @return associated IType JavaModel element or null if this role doesn't have a bound base class
	 */    
	public IType getBaseClass() throws JavaModelException;
	
	public String getBaseclassName();
	
	/** If the base type is anchored, represent it using '<@..>' */
	public String getFullBaseclassName();
	
	/**
	 * Returns true if this Role Type is defined in a Role File.
	 * @return
	 */
	public boolean isRoleFile();
	
	/**
	 * Get all tsuper roles of this role.
	 * If multiple tsuper roles exist (due to team nesting) the resulting array
	 * will list those first that are induced by inner team inheritance and going out from there.
	 *  
	 * @return an array of tsuper roles or null;
	 * @throws JavaModelException thrown if some type lookup failed
	 * @since 1.2.8
	 */
	public IType[] getTSuperRoles() throws JavaModelException;

}
